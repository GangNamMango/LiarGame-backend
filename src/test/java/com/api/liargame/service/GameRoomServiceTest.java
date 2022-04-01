package com.api.liargame.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api.liargame.constants.SettingConstant;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.DuplicateUserNicknameException;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.repository.GameRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class GameRoomServiceTest {

  @Autowired
  GameRoomService gameRoomService;
  @Autowired
  GameRoomRepository gameRoomRepository;

  GameRoom gameRoom;

  @BeforeEach
  void beforeEach() {
    User user = User.builder()
        .nickname("user1")
        .character("ch1")
        .role(Role.HOST)
        .build();
    Setting setting = new Setting();
    gameRoom = GameRoom.builder()
        .host(user)
        .settings(setting)
        .build();
  }


  @Test
  @DisplayName("게임 방을 생성할 수 있어야 한다.")
  void create(){
    UserRequestDto userRequestDto = new UserRequestDto("user1", "ch1");

    GameRoom room = gameRoomService.createRoom(userRequestDto);

    assertThat(room.getRoomId().length()).isEqualTo(5);
    assertThat(room.getHost().getNickname()).isEqualTo("user1");
    assertThat(room.getSettings().getTimeLimit()).isEqualTo(SettingConstant.DEFAULT_TIME_LIMIT);
    assertThat(room.getUsers().size()).isEqualTo(1);
  }


  @Test
  @DisplayName("게임 방에 입장할 수 있어야 한다.")
  void enter() {
    //게임 방 저장
    String roomId = gameRoomRepository.save(gameRoom);

    //유저 요청 생성
    UserRequestDto userRequestDto = new UserRequestDto("user2", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);

    //방 입장
    GameRoom gameRoomResult = gameRoomService.enter(enterRequestDto);

    // 결과
    assertThat(gameRoomResult).isSameAs(gameRoom);
    assertThat(gameRoomResult.getUsers().size()).isEqualTo(2);
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
    //게임 방 저장
    String roomId = gameRoomRepository.save(gameRoom);

    //유저 요청 생성
    UserRequestDto userRequestDto = new UserRequestDto("user1", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);

    // 결과
    assertThrows(DuplicateUserNicknameException.class,
        () -> gameRoomService.enter(enterRequestDto));
  }
}
