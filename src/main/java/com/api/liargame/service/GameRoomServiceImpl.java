package com.api.liargame.service;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.CounterResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.repository.GameRoomRepository;
import com.api.liargame.repository.UserRepository;
import com.api.liargame.repository.WordRepository;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService {

  private final GameRoomRepository gameRoomRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate webSocket;
  private final WordRepository wordRepository;

  @Override
  public GameRoom createRoom(UserRequestDto userRequestDto) {
    String roomId = createGameRoomId();
    String nickname = userRequestDto.getNickname();
    String character = userRequestDto.getCharacter();
    User user = User.builder()
        .nickname(nickname)
        .character(character)
        .role(Role.HOST)
        .build();
    Setting defaultSetting = new Setting();
    GameRoom gameRoom = new GameRoom(roomId, user, defaultSetting);
    userRepository.save(user);
    gameRoomRepository.save(gameRoom);

    return gameRoom;
  }

  @Override
  public User enter(EnterRequestDto enterRequestDto) {
    String roomId = enterRequestDto.getRoomId();
    GameRoom foundGameRoom = gameRoomRepository.findById(roomId);
    if (foundGameRoom == null) {
      throw new NotFoundGameRoomException();
    }

    User user = enterRequestDto.getUser().toEntity();
    userRepository.save(user);

    foundGameRoom.addUser(user);
    foundGameRoom.update();



    return user;
  }

  @Override
  public GameRoom find(String roomId) {
    return gameRoomRepository.findById(roomId);
  }

  @Override
  public User leave(String roomId, String userId) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);
    User user = userRepository.findById(userId);

    if (user == null)
      throw new IllegalStateException("유저를 찾을 수 없습니다.");

    if (user.getRole() == Role.HOST) {
      Optional<User> nextHost = gameRoom
          .getUsers()
          .stream()
          .filter(u -> !u.getId().equals(userId))
          .findAny();

      if (nextHost.isPresent())
        gameRoom.changeHost(nextHost.get());
      else {
        gameRoomRepository.delete(gameRoom.getRoomId());
      }
    }

    gameRoom.deleteUser(user);
    gameRoom.update();

    userRepository.delete(user.getId());

    return user;
  }

  @Override
  public User updateUserProfile(UpdateProfileRequestDto updateProfileRequestDto) {
    GameRoom gameRoom = gameRoomRepository.findById(updateProfileRequestDto.getRoomId());
    if (gameRoom == null)
      throw new NotFoundGameRoomException("방이 존재하지 않습니다.");

    User user = gameRoom.getUsers()
        .stream()
        .filter(u -> u.getId().equals(updateProfileRequestDto.getUserId()))
        .findAny()
        .orElse(null);
    if (user == null)
        throw new IllegalStateException("대기실에 존재하지 않는 유저입니다.");

    user.setNickname(updateProfileRequestDto.getNickname());
    user.setCharacter(updateProfileRequestDto.getCharacter());
    user.update();
    gameRoom.update();

    return user;
  }

  @Override
  public Info createGameInfo(String roomId, String userId) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);
    if (gameRoom == null)
      throw new NotFoundGameRoomException();

    gameRoom.validateHost(userId);

    String topic = gameRoom.getSetting().getTopic();
    String word;

    if (topic.equals("random")) {
      word = wordRepository.findWordByRandomTopic();
    } else {
      word = wordRepository.findWordByTopic(topic);
    }

    User liar = getRandomLiar(gameRoom);

    Info gameInfo = Info.create(liar, topic, word);
    gameRoom.setInfo(gameInfo);

    return gameInfo;
  }

  public User getRandomLiar(GameRoom gameRoom) {
    ArrayList<User> users = new ArrayList<>(gameRoom.getUsers());

    int randomIndex = new Random().nextInt(users.size());
    return users.get(randomIndex);
  }

  @Override
  public String createGameRoomId() {
    while (true) {
      String roomId = randomRoomId();
      GameRoom existRoomId = gameRoomRepository.findById(roomId);
      if (existRoomId == null) {
        return roomId;
      }
    }
  }

  @Override
  public String randomRoomId() {
    int length = 5;
    String str = "qwertyuiopasdfghjklzxcvbnm123456789";
    String roomId = "";
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      roomId += str.charAt(random.nextInt(str.length()));
    }

    return roomId;
  }

  @Override
  public void gameCountdown(String roomId) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);
    long delay = 1000L; // 1초후 실행
    long period = 1000L; // 1초마다 실행
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
    long time = gameRoom.getSetting().getTimeLimit();

    CounterResponseDto counterResponseDto= new CounterResponseDto(time);

      ResponseDto<CounterResponseDto> response = ResponseDto.<CounterResponseDto>builder()
        .status(ResponseStatus.SUCCESS)
        .message("게임 진행 중..")
        .data(counterResponseDto)
        .build();

      public void run() {
        if (time-- > 0) {
          counterResponseDto.setCount(time);
          webSocket.convertAndSend("/sub/game/" + gameRoom.getRoomId() + "/countdown",response);
        } else {
          timer.cancel();
        }
      }
    };
    timer.scheduleAtFixedRate(task, delay, period);
  }
}
