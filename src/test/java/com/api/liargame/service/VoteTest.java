package com.api.liargame.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api.liargame.constants.GameRoomConstant;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.global.SlackLogger;
import com.api.liargame.repository.GameRoomRepository;
import com.api.liargame.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class VoteTest {

  @Autowired
  GameRoomService gameRoomService;
  @Autowired
  UserRepository userRepository;
  @Autowired
  GameRoomRepository gameRoomRepository;
  @MockBean
  SlackLogger slackLogger;


  User host;
  GameRoom gameRoom;

  @BeforeEach
  void beforeEach() {
    userRepository.clear();
    gameRoomRepository.clear();

    host = new User("host", Role.HOST, "ch1");
    gameRoom = new GameRoom("roomId", host, new Setting());

    userRepository.save(host);
    gameRoomRepository.save(gameRoom);
  }

  User createUser() {
    User user = new User(UUID.randomUUID().toString(), Role.GUEST, "0");
    userRepository.save(user);

    return user;
  }

  @Test
  @DisplayName("투표가능")
  void vote_success() {
    for (int i=0; i< GameRoomConstant.ROOM_MIN_USER; i++) {
      User user = createUser();
      gameRoom.addUser(user);
    }


    gameRoomService.createGameInfo("roomId", host.getId());
    gameRoom.setGameStatus(GameStatus.VOTE);
    gameRoomService.vote(gameRoom.getRoomId(), host.getId(), "host");

    assertThat(host.isVote()).isEqualTo(true);
    assertThat(host.getVoteCount()).isEqualTo(1);
    assertThat(gameRoom.getVoteCompleteCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("이미 투표한 유저는 투표 할 수 없어야 한다.")
  void vote_fail_when_already_vote() {
    for (int i=0; i< GameRoomConstant.ROOM_MIN_USER; i++) {
      User user = createUser();
      gameRoom.addUser(user);
    }

    gameRoomService.createGameInfo("roomId", host.getId());
    gameRoom.setGameStatus(GameStatus.VOTE);
    gameRoomService.vote(gameRoom.getRoomId(), host.getId(), "host");

    assertThrows(IllegalStateException.class, () -> {
      gameRoomService.vote(gameRoom.getRoomId(), host.getId(), "host");
    });
  }

  @Test
  @DisplayName("게임 상태가 VOTE일때만 투표가 가능하다.")
  void check_game_status() {
    User user = new User("user", Role.GUEST, "ch1");
    userRepository.save(user);
    gameRoom.addUser(user);

    assertThrows(IllegalStateException.class, () -> {
      gameRoomService.vote(gameRoom.getRoomId(), user.getId(), "host");
    });
  }
}
