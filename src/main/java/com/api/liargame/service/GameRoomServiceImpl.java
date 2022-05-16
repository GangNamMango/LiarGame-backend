package com.api.liargame.service;

import com.api.liargame.controller.dto.request.ChoiceRequestDto;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.CounterResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.GameRole;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.repository.GameRoomRepository;
import com.api.liargame.repository.UserRepository;
import com.api.liargame.repository.WordRepository;
import java.util.ArrayList;
import java.util.List;
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

    if (user == null) {
      throw new IllegalStateException("유저를 찾을 수 없습니다.");
    }

    if (user.getRole() == Role.HOST) {
      Optional<User> nextHost = gameRoom
          .getUsers()
          .stream()
          .filter(u -> !u.getId().equals(userId))
          .findAny();

      if (nextHost.isPresent()) {
        gameRoom.changeHost(nextHost.get());
      } else {
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
    if (gameRoom == null) {
      throw new NotFoundGameRoomException("방이 존재하지 않습니다.");
    }

    User user = isExistUserInGame(updateProfileRequestDto.getUserId(), gameRoom);

    user.setNickname(updateProfileRequestDto.getNickname());
    user.setCharacter(updateProfileRequestDto.getCharacter());
    user.update();
    gameRoom.update();

    return user;
  }

  @Override
  public Info createGameInfo(String roomId, String userId) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);
    if (gameRoom == null) {
      throw new NotFoundGameRoomException();
    }

    gameRoom.validateHost(userId);

    String topic = gameRoom.getSetting().getTopic();
    String word = wordRepository.findWordByTopic(topic);

    User liar = getRandomLiar(gameRoom);
    liar.setGameRole(GameRole.LIAR);

    Info gameInfo = Info.create(liar, topic, word);
    gameRoom.setInfo(gameInfo);

    gameRoom.setGameStatus(GameStatus.PROGRESS);

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

  /*
    컨트롤러의 역할같은 느낌이라 프록시를 써서 Run부분을 떄어내서
    컨트롤러에서 run 부분만 보이게 하는 방식으로 구현하는게 좋아 보입니다.
    프록시 패턴..?
  */
  @Override
  public void gameCountdown(String roomId, String event) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);
    long delay = 1000L; // 1초후 실행
    long period = 1000L; // 1초마다 실행
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      long time = gameRoom.getSetting().getTimeLimit();

      CounterResponseDto counterResponseDto = new CounterResponseDto(time,
          gameRoom.getGameStatus().name());

      ResponseDto<CounterResponseDto> response = ResponseDto.<CounterResponseDto>builder()
          .status(ResponseStatus.SUCCESS)
          .message("게임 진행 중")
          .data(counterResponseDto)
          .build();

      public void run() {
        if (time < 1) {
          counterResponseDto.setGameStatus(GameStatus.VOTE.name());
          gameRoom.setGameStatus(GameStatus.VOTE);
          timer.cancel();
        }
        counterResponseDto.setCount(time);
        webSocket.convertAndSend(event, response);
        time--;
      }
    };
    timer.scheduleAtFixedRate(task, delay, period);
  }

  @Override
  public GameRoom vote(String roomId, String userId, String voteTo) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);
    if (gameRoom == null) {
      throw new NotFoundGameRoomException("방이 존재하지 않습니다.");
    }

    if (!gameRoom.getGameStatus().equals(GameStatus.VOTE)) {
      throw new IllegalStateException("현재 투표 진행 중이 아닙니다.");
    }

    //투표자 유저 검증
    User user = isExistUserInGame(userId, gameRoom);
    if (user.isVote()) {
      throw new IllegalStateException("이미 투표한 유저입니다.");
    }

    //피투표자? 유저 검증
    User votedUser = gameRoom.getUsers().stream()
        .filter(u -> u.getNickname().equals(voteTo))
        .findAny()
        .orElseThrow(() -> {
          throw new IllegalArgumentException("존재하지 않는 유저에게 투표할 수 없습니다." + voteTo);
        });

    //투표 처리
    gameRoom.vote(user, votedUser);

    return gameRoom;
  }


  private User isExistUserInGame(String userId, GameRoom gameRoom) {
    return gameRoom.getUsers()
        .stream()
        .filter(u -> u.getId().equals(userId))
        .findAny()
        .orElseThrow(() -> {
          throw new IllegalArgumentException("게임 방에 존재하지 않는 유저입니다.");
        });
  }

  private void isLiar(GameRoom gameRoom,String liarId) {
    User realLiar = gameRoom.getInfo().getLiar();
    if (!realLiar.getId().equals(liarId))
      throw new IllegalStateException("당신은 라이어가 아닙니다.");
  }

  private boolean isSame(GameRoom gameRoom, String liarWord) {
    String gameRoomWord = gameRoom.getInfo().getWord();
    return gameRoomWord.equals(liarWord);
  }

  @Override
  public boolean checkVoteComplete(String roomId) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);

    return gameRoom.getVoteCompleteCount() == gameRoom.getUsers().size();
  }

  @Override
  public boolean checkAnswer(ChoiceRequestDto choiceRequestDto) {
    GameRoom gameRoom = gameRoomRepository.findById(choiceRequestDto.getRoomId());
    if (gameRoom == null)
      throw new NotFoundGameRoomException("방이 존재하지 않습니다.");

    String liar = choiceRequestDto.getUserId();
    String requestedWord = choiceRequestDto.getChoice();

    isLiar(gameRoom, liar);
    return isSame(gameRoom, requestedWord);
  }
}
