package com.api.liargame.service;

import static com.api.liargame.domain.User.GameRole.LIAR;
import static com.api.liargame.domain.User.GameRole.MEMBER;

import com.api.liargame.constants.GameRoomConstant;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.CounterResponseDto;
import com.api.liargame.controller.dto.response.GameResultResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.global.SlackLogger;
import com.api.liargame.repository.GameRoomRepository;
import com.api.liargame.repository.UserRepository;
import com.api.liargame.repository.WordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameRoomServiceImpl implements GameRoomService {

  private final GameRoomRepository gameRoomRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate webSocket;
  private final WordRepository wordRepository;
  private final SlackLogger slackLogger;

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
    GameRoom foundGameRoom = getGameRoomOrFail(roomId);

    GameStatus gameStatus = foundGameRoom.getGameStatus();
    if (!gameStatus.equals(GameStatus.WAITING)) {
      throw new IllegalStateException("????????? ????????? ???????????? ?????? ????????? ??? ????????????. ?????? ?????? : " + gameStatus);
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
  public User leave(GameRoom gameRoom, String userId) {
    User user = isExistUserInGame(userId, gameRoom);

    GameStatus gameStatus = gameRoom.getGameStatus();

    if (gameStatus.equals(GameStatus.WAITING)) {
      leaveWhenWaitingRoom(gameRoom, user);
    } else {
      leaveWhenInGame(gameRoom, user);
    }

    gameRoom.deleteUser(user);
    gameRoom.update();
    userRepository.delete(user.getId());

    return user;
  }

  private void leaveWhenInGame(GameRoom gameRoom, User user) {
    if (user.getGameRole().equals(LIAR)
        || gameRoom.getUserCount() - 1 < GameRoomConstant.ROOM_MIN_USER) {
      gameRoom.setGameStatus(GameStatus.END);
    }
  }

  private void leaveWhenWaitingRoom(GameRoom gameRoom, User user) {
    if (user.getRole() == Role.HOST) {
      Optional<User> nextHost = gameRoom
          .getUsers()
          .stream()
          .filter(u -> !u.getId().equals(user.getId()))
          .findAny();

      if (nextHost.isPresent()) {
        gameRoom.changeHost(nextHost.get());
      } else {
        gameRoomRepository.delete(gameRoom.getRoomId());
      }
    }
  }

  @Override
  public User updateUserProfile(UpdateProfileRequestDto updateProfileRequestDto) {
    GameRoom gameRoom = getGameRoomOrFail(updateProfileRequestDto.getRoomId());

    User user = isExistUserInGame(updateProfileRequestDto.getUserId(), gameRoom);

    user.setNickname(updateProfileRequestDto.getNickname());
    user.setCharacter(updateProfileRequestDto.getCharacter());
    user.update();
    gameRoom.update();

    return user;
  }

  @Override
  public Info createGameInfo(String roomId, String userId) {
    GameRoom gameRoom = getGameRoomOrFail(roomId);

    gameRoom.validateHost(userId);
    checkMinUser(gameRoom);

    String topic = wordRepository.resetTopic(gameRoom.getSetting().getTopic());
    String word = wordRepository.findWordByTopic(topic);

    User liar = getRandomLiar(gameRoom);
    liar.setGameRole(LIAR);

    Info gameInfo = Info.create(liar, topic, word);
    gameRoom.setInfo(gameInfo);

    gameRoom.setGameStatus(GameStatus.PROGRESS);

    slackLogger.send(
        String.format("[??????????????????] ????????? ??????????????????. (??? ?????? : %s, ?????? ?????? : %d)", gameRoom.getRoomId(),
            gameRoom.getUserCount()));

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
    Integer length = GameRoomConstant.ROOM_ID_LENGTH;
    String roomId = "";
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      roomId += random.nextInt(10);
    }
    return roomId;
  }

  @Override
  public void gameCountdown(String roomId, String event) {
    GameRoom gameRoom = getGameRoomOrFail(roomId);
    long delay = 1000L; // 1?????? ??????
    long period = 1000L; // 1????????? ??????
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      long time = gameRoom.getSetting().getTimeLimit();

      CounterResponseDto counterResponseDto = new CounterResponseDto(time,
          gameRoom.getGameStatus().name());

      ResponseDto<CounterResponseDto> response = ResponseDto.<CounterResponseDto>builder()
          .status(ResponseStatus.SUCCESS)
          .message("?????? ?????? ???")
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
    GameRoom gameRoom = getGameRoomOrFail(roomId);

    if (!gameRoom.getGameStatus().equals(GameStatus.VOTE)) {
      throw new IllegalStateException("?????? ?????? ?????? ?????? ????????????.");
    }

    //????????? ?????? ??????
    User user = isExistUserInGame(userId, gameRoom);
    if (user.isVote()) {
      throw new IllegalStateException("?????? ????????? ???????????????.");
    }

    //????????????? ?????? ??????
    User votedUser = gameRoom.getUsers().stream()
        .filter(u -> u.getNickname().equals(voteTo))
        .findAny()
        .orElseThrow(() -> {
          throw new IllegalArgumentException("???????????? ?????? ???????????? ????????? ??? ????????????." + voteTo);
        });

    //?????? ??????
    gameRoom.vote(user, votedUser);

    return gameRoom;
  }

  private User isExistUserInGame(String userId, GameRoom gameRoom) {
    User user = userRepository.findById(userId);
    if (user == null) {
      throw new IllegalArgumentException("????????? ?????? ??? ????????????.");
    }

    return gameRoom.getUsers()
        .stream()
        .filter(u -> u.getId().equals(userId))
        .findAny()
        .orElseThrow(() -> {
          throw new IllegalArgumentException("?????? ?????? ???????????? ?????? ???????????????.");
        });
  }

  private void isLiar(GameRoom gameRoom, String liarId) {
    User realLiar = gameRoom.getInfo().getLiar();
    if (!realLiar.getId().equals(liarId)) {
      throw new IllegalStateException("????????? ???????????? ????????????.");
    }
  }

  private boolean isSame(GameRoom gameRoom, String liarWord) {
    String gameRoomWord = gameRoom.getInfo().getWord();
    return gameRoomWord.equals(liarWord);
  }


  @Override
  public boolean checkVoteComplete(String roomId) {
    GameRoom gameRoom = getGameRoomOrFail(roomId);

    return gameRoom.getVoteCompleteCount() == gameRoom.getUsers().size();
  }

  @Override
  public boolean checkAnswer(String roomId, String userId, String choice) {
    GameRoom gameRoom = getGameRoomOrFail(roomId);

    isLiar(gameRoom, userId);
    return isSame(gameRoom, choice);
  }

  @Override
  public GameResultResponseDto getGameResult(String roomId, String userId, String choice) {
    GameRoom gameRoom = getGameRoomOrFail(roomId);
    User liar = gameRoom.getLiar();

    if (liar == null) {
      throw new IllegalStateException("?????? ????????? ???????????? ???????????? ????????????.");
    }

    switch (gameRoom.getGameStatus()) {
      case VOTE:
        return getVoteWinner(gameRoom, liar);
      case CHOICE:
        return getChoiceWinner(gameRoom, userId, choice, liar);
      default:
        throw new IllegalStateException("?????? ????????? VOTE??? CHOICE???????????? ????????? ??? ????????????.");
    }
  }

  private GameResultResponseDto getChoiceWinner(GameRoom gameRoom, String userId, String choice,
      User liar) {
    //???????????? ??????
    if (checkAnswer(gameRoom.getRoomId(), userId, choice)) {
      processEndGame(gameRoom);
      return GameResultResponseDto.ofLiarWin(
          GameStatus.END,
          LIAR,
          liar.getNickname(),
          choice
      );
    } else {
      //???????????? ??????
      processEndGame(gameRoom);
      return GameResultResponseDto.ofMemberWin(
          GameStatus.END,
          MEMBER,
          liar.getNickname(),
          choice,
          gameRoom.getInfo().getWord()
      );
    }
  }

  private GameResultResponseDto getVoteWinner(GameRoom gameRoom, User liar) {
    //?????? ????????? ??????
    List<User> users = new ArrayList<>(gameRoom.getUsers());
    users.sort((o1, o2) -> o2.getVoteCount() - o1.getVoteCount());
    int maxVoteCount = users.get(0).getVoteCount();

    //?????? ?????? ???????????? ?????? ????????????.
    List<User> votedUsers = users.stream()
        .filter(u -> u.getVoteCount() >= maxVoteCount)
        .collect(Collectors.toList());

    //votedUsers ?????? ???????????? ????????? CHOICE ????????? ???????????? ??????.
    if (votedUsers.stream().anyMatch(User::isLiar)) {
      gameRoom.setGameStatus(GameStatus.CHOICE);
      return GameResultResponseDto.ofLiarChoice(
          GameStatus.CHOICE,
          liar.getNickname(),
          liar.getVoteCount());
    } else {
      //votedUsers?????? ???????????? ????????? ???????????? ????????? ?????????.
      processEndGame(gameRoom);
      return GameResultResponseDto.ofLiarWin(
          GameStatus.END,
          LIAR,
          liar.getNickname(),
          gameRoom.getInfo().getWord());
    }
  }

  private void checkMinUser(GameRoom gameRoom) {
    Integer totalMember = gameRoom.getUsers().size();
    if (totalMember < GameRoomConstant.ROOM_MIN_USER) {
      throw new IllegalStateException(
          "????????? ???????????? ?????? ?????? ????????? " + GameRoomConstant.ROOM_MIN_USER + "??? ?????????.");
    }
  }

  @Override
  public void processEndGame(GameRoom gameRoom) {
    log.info("[???????????? ??????] ????????? ?????????????????????. (CODE : {})", gameRoom.getRoomId());
    slackLogger.send(String.format("[???????????? ??????] ????????? ?????????????????????. (??? ?????? : %s)", gameRoom.getRoomId()));

    gameRoom.setGameStatus(GameStatus.END);

    clearUsersInGame(gameRoom);

    //??? ??????
    gameRoomRepository.delete(gameRoom.getRoomId());
  }

  private void clearUsersInGame(GameRoom gameRoom) {
    gameRoom.getUsers()
        .forEach(u -> userRepository.delete(u.getId()));
  }

  @Override
  public GameRoom getGameRoomOrFail(String roomId) {
    GameRoom gameRoom = gameRoomRepository.findById(roomId);

    if (gameRoom == null) {
      throw new NotFoundGameRoomException("?????? ?????? ?????? ??? ????????????.");
    }

    return gameRoom;
  }
}
