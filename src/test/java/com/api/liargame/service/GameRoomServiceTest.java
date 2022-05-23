package com.api.liargame.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import com.api.liargame.constants.GameRoomConstant;
import com.api.liargame.constants.SettingConstant;
import com.api.liargame.controller.dto.request.ChoiceRequestDto;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.DuplicateUserNicknameException;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.repository.GameRoomRepository;
import com.api.liargame.repository.UserRepository;
import com.api.liargame.repository.WordRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.GreaterThan;
import org.mockito.internal.matchers.LessThan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class GameRoomServiceTest {

  @Autowired
  GameRoomService gameRoomService;
  @Autowired
  GameRoomRepository gameRoomRepository;
  @Autowired
  UserRepository userRepository;

  @Autowired
  WordRepository wordRepository;

  GameRoom createRoom() {
    User user = User.builder()
        .nickname("user1")
        .character("ch1")
        .role(Role.HOST)
        .build();
    Setting setting = new Setting();
    return GameRoom.builder()
        .roomId("roomID")
        .host(user)
        .setting(setting)
        .build();
  }


  @Test
  @DisplayName("게임 방을 생성할 수 있어야 한다.")
  void create(){
    UserRequestDto userRequestDto = new UserRequestDto("user1", "ch1");

    GameRoom room = gameRoomService.createRoom(userRequestDto);

    assertThat(room.getRoomId().length()).isEqualTo(GameRoomConstant.ROOM_ID_LENGTH);
    assertThat(room.getHost().getNickname()).isEqualTo("user1");
    assertThat(room.getSetting().getTimeLimit()).isEqualTo(SettingConstant.DEFAULT_TIME_LIMIT);
    assertThat(room.getUsers().size()).isEqualTo(1);
  }


  @Test
  @DisplayName("게임 방에 입장할 수 있어야 한다.")
  void enter() {
    GameRoom gameRoom = createRoom();
    //게임 방 저장
    String roomId = gameRoomRepository.save(gameRoom);
    //유저 요청 생성
    UserRequestDto userRequestDto = new UserRequestDto("user2", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);

    //방 입장
    User enteredUser = gameRoomService.enter(enterRequestDto);
    GameRoom foundGameRoom = gameRoomRepository.findById(roomId);
    User foundUser = foundGameRoom.getUsers()
        .stream().filter(u -> u.getRole().equals(Role.GUEST))
        .findAny()
        .get();

    // 결과
    assertThat(gameRoom.getUsers().size()).isEqualTo(2);
    assertThat(foundUser.getId()).isEqualTo(enteredUser.getId());
  }

  @Test
  @DisplayName("방이 존재하지 않으면 입장할 수 없어야 한다.")
  void fail_when_not_exist_room() {
    //유저 요청 생성
    UserRequestDto userRequestDto = new UserRequestDto("user1", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto("test", userRequestDto);

    assertThrows(NotFoundGameRoomException.class, () -> gameRoomService.enter(enterRequestDto));
  }

  @Test
  @DisplayName("중복된 닉네임은 방에 입장할 수 없어야 한다.")
  void fail_when_duplicate_nickname() {
    GameRoom gameRoom = createRoom();
    //게임 방 저장
    String roomId = gameRoomRepository.save(gameRoom);

    //유저 요청 생성
    UserRequestDto userRequestDto = new UserRequestDto("user1", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);

    // 결과
    assertThrows(DuplicateUserNicknameException.class,
        () -> gameRoomService.enter(enterRequestDto));
  }

  @Test
  @DisplayName("게임 방에서 나갈 수 있어야 한다.")
  void leave() {
    GameRoom gameRoom = createRoom();
    User user = new User("test", Role.GUEST, "Ch1");

    String roomId = gameRoomRepository.save(gameRoom);
    userRepository.save(user);

    gameRoom.addUser(user);
    User leavedUser = gameRoomService.leave(roomId, user.getId());

    GameRoom updatedGameRoom = gameRoomRepository.findById(roomId);
    User foundUser = userRepository.findById(user.getId());

    assertThat(leavedUser.getId()).isEqualTo(user.getId());
    assertThat(updatedGameRoom.getUsers().size()).isEqualTo(1);
    assertThat(foundUser).isNull();
  }

  @Test
  @DisplayName("방장이 나갔을 경우 방장이 변경되어야 한다.")
  void leave_host() {
    User host = new User("host", Role.HOST, "ch0");
    User user = new User("test", Role.GUEST, "ch1");
    User user2 = new User("test2", Role.GUEST, "ch2");
    GameRoom gameRoom = new GameRoom("roomId", host, new Setting());

    userRepository.save(host);
    userRepository.save(user);
    userRepository.save(user2);
    gameRoomRepository.save(gameRoom);

    gameRoom.addUser(user);
    gameRoom.addUser(user2);

    String originalHostId = gameRoom.getHost().getId();
    gameRoomService.leave(gameRoom.getRoomId(), originalHostId);

    GameRoom foundGameRoom = gameRoomRepository.findById(gameRoom.getRoomId());
    User leavedUser = userRepository.findById(originalHostId);

    assertThat(foundGameRoom.getHost().getId()).isNotEqualTo(originalHostId);
    assertThat(foundGameRoom.getUsers().size()).isEqualTo(2);
    assertThat(foundGameRoom.getHost().getRole()).isEqualTo(Role.HOST);
    assertThat(leavedUser).isNull();
  }

  @Test
  @DisplayName("대기실 인원이 1명일 경우 방은 삭제되어야 한다.")
  void leave_host_and_delete_room() {
    User host = new User("host", Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom("roomId", host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    User leavedUser = gameRoomService.leave(gameRoom.getRoomId(), host.getId());
    GameRoom foundGameRoom = gameRoomRepository.findById(gameRoom.getRoomId());
    User foundUser = userRepository.findById(host.getId());

    assertThat(leavedUser.getId()).isEqualTo(host.getId());
    assertThat(foundGameRoom).isNull();
    assertThat(foundUser).isNull();
  }

  @Test
  @DisplayName("프로필 변경이 가능해야한다.")
  void update_profile() {
    String roomId = "test-room-id";

    User host = new User("test", Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    UpdateProfileRequestDto updateProfileRequestDto = new UpdateProfileRequestDto(
        roomId,
        host.getId(),
        "changeNickname",
        "ch2");

    User changedUser = gameRoomService.updateUserProfile(updateProfileRequestDto);

    assertThat(changedUser.getNickname()).isEqualTo(updateProfileRequestDto.getNickname());
    assertThat(changedUser.getCharacter()).isEqualTo(updateProfileRequestDto.getCharacter());
  }

  @Test
  @DisplayName("null값이 들어올경우 기존값을 유지해야 한다.")
  void update_profile_with_null() {
    String roomId = "test-room-id";
    String userId = "test";

    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    UpdateProfileRequestDto updateProfileRequestDto = new UpdateProfileRequestDto(
        roomId,
        host.getId(),
        null,
        "ch2");

    User changedUser = gameRoomService.updateUserProfile(updateProfileRequestDto);

    assertThat(changedUser.getNickname()).isEqualTo(userId);
    assertThat(changedUser.getCharacter()).isEqualTo(updateProfileRequestDto.getCharacter());
  }

  @Test
  @DisplayName("게임 정보를 생성할 수 있어야 한다.")
  void gameInfo() {
    String roomId = "test-room-id";
    String userId = "test";

    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    User user1 = new User("user1", Role.GUEST, "ch0");
    User user2 = new User("user2", Role.GUEST, "ch0");
    userRepository.save(user1);
    userRepository.save(user2);

    gameRoom.addUser(user1);
    gameRoom.addUser(user2);

    Info gameInfo = gameRoomService.createGameInfo(roomId, host.getId());

    assertThat(gameInfo.getLiar()).isNotNull();
    assertThat(gameInfo.getTopic()).isNotNull();
    assertThat(gameInfo.getWord()).isNotNull();
  }

  @Test
  @DisplayName("호스트가 아니면 게임정보를 생성할 수 없다.")
  void gameInfo_fail_when_not_host() {
    String roomId = "test-room-id";
    String userId = "test";

    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    User guest = new User("test-2", Role.HOST, "ch0");
    userRepository.save(guest);
    gameRoom.addUser(guest);

    assertThrows(IllegalStateException.class, () -> gameRoomService.createGameInfo(roomId, guest.getId()));
  }


  @Test
  @DisplayName("해당 방의 라이어가 아니면 에러가 나야한다.")
  void fail_when_not_liar() {
    String roomId = "test-room-id";
    String userId = "test";

    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    User guest = new User("test-2", Role.GUEST, "ch0");
    userRepository.save(guest);
    gameRoom.addUser(guest);

    User liar = new User("liar", Role.GUEST, "ch0");
    userRepository.save(liar);
    gameRoom.addUser(liar);

    String topic = wordRepository.resetTopic(gameRoom.getSetting().getTopic());
    String word = wordRepository.findWordByTopic(topic);
    Info info = Info.create(liar, topic, word);
    gameRoom.setInfo(info);

    assertThrows(IllegalStateException.class, () -> gameRoomService.checkAnswer(roomId, userId, word));
  }

  @Test
  @DisplayName("해당 방의 제시어와 같아야한다.")
  void compare_word() {
    String roomId = "test-room-id";
    String userId = "test";
    String gameRoomWord = "word";
    String sameWord = "word";
    String anotherWord = "word2";
    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    String topic = gameRoom.getSetting().getTopic();
    Info info = Info.create(host, topic, gameRoomWord);

    gameRoom.setInfo(info);
    //    assertThat(gameRoomService.isSame(gameRoom, sameWord)).isEqualTo(true);
    //    assertThat(gameRoomService.isSame(gameRoom, anotherWord)).isEqualTo(false);
  }

  @Test
  @DisplayName("게임 시작 후 방에 입장할 수 없어야 한다.")
  void enter_fail_when_game_start() {
    String roomId = "test-room-id";
    String userId = "test";
    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);

    gameRoom.setGameStatus(GameStatus.PROGRESS);

    assertThrows(IllegalStateException.class, () -> {
      gameRoomService.enter(new EnterRequestDto(roomId, new UserRequestDto("user2", "ch1")));
    });
  }

  @Test
  @DisplayName("방의 아이디는 숫자로만 이루어져 있어야 한다.")
  void randomRoomIdTest() {
    String reg = "^[0-9]{" + GameRoomConstant.ROOM_ID_LENGTH + "}";
    String roomId = gameRoomService.randomRoomId();
    assertTrue(Pattern.matches(reg, roomId));
  }


  @Test
  @DisplayName("방의 유저의 최소 인원수 미만이면 에러가 발생해야한다.")
  void checkMinUserTest() {
    String roomId = "test-room-id";
    String userId = "test";

    User host = new User(userId, Role.HOST, "ch0");
    GameRoom gameRoom = new GameRoom(roomId, host, new Setting());
    userRepository.save(host);
    gameRoomRepository.save(gameRoom);
    assertThrows(IllegalStateException.class, () -> gameRoomService.createGameInfo(roomId, host.getId()));

    for (int i = 0; i < GameRoomConstant.ROOM_MIN_USER - 2; i++) {
      String testId = "test-" + Integer.toString(i);
      User guest = new User(testId, Role.GUEST, "ch0");
      userRepository.save(guest);
      gameRoom.addUser(guest);
      assertThrows(IllegalStateException.class, () -> gameRoomService.createGameInfo(roomId, host.getId()));
    }
    assertTrue(GameRoomConstant.ROOM_MIN_USER > gameRoom.getUsers().size());
    //정상 동작
    User guest = new User("lastUser", Role.GUEST, "ch0");
    userRepository.save(guest);
    gameRoom.addUser(guest);
    gameRoomService.createGameInfo(roomId, host.getId());

    assertTrue(GameRoomConstant.ROOM_MIN_USER <= gameRoom.getUsers().size());
  }
}

