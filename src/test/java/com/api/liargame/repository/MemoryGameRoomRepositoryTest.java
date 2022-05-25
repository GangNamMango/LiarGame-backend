package com.api.liargame.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.global.SlackLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemoryGameRoomRepositoryTest {

  @InjectMocks
  MemoryGameRoomRepository gameRoomRepository;

  @Mock
  SlackLogger slackLogger;

  @BeforeEach
  void repository_clear() {
    gameRoomRepository.clear();

    Mockito.mock(SlackLogger.class);

    gameRoomRepository = new MemoryGameRoomRepository(slackLogger);
  }

  @Test
  @DisplayName("게임 방을 저장할 수 있어야 한다.")
  void save() {
    User user1 = createUser("user1", "ch1", Role.HOST);
    User user2 = createUser("user2", "ch2", Role.GUEST);
    Setting setting = new Setting();

    GameRoom gameRoom = GameRoom.builder()
        .host(user1)
        .setting(setting)
        .build();

    gameRoom.addUser(user2);

    String roomId = gameRoomRepository.save(gameRoom);
    GameRoom savedGameRoom = gameRoomRepository.findById(roomId);

    assertThat(roomId).isEqualTo(gameRoom.getRoomId());
    assertThat(savedGameRoom.getUsers().size()).isEqualTo(2);
    assertThat(savedGameRoom.getHost()).isSameAs(user1);
  }

  @Test
  @DisplayName("게임 방을 찾을 수 있어야 한다.")
  void findById() {
    User user1 = createUser("user1", "ch1", Role.HOST);
    Setting setting = new Setting();

    GameRoom gameRoom = GameRoom.builder()
        .host(user1)
        .setting(setting)
        .build();

    gameRoomRepository.save(gameRoom);

    GameRoom foundGameRoom = gameRoomRepository.findById(gameRoom.getRoomId());

    assertThat(foundGameRoom).isSameAs(gameRoom);
    assertThat(gameRoomRepository.findById("xxx")).isNull();
  }

  @Test
  @DisplayName("게임 방을 삭제할 수 있어야 한다.")
  public void delete() {
    User user1 = createUser("user1", "ch1", Role.HOST);
    Setting setting = new Setting();

    GameRoom gameRoom = GameRoom.builder()
        .host(user1)
        .setting(setting)
        .build();

    String roomId = gameRoomRepository.save(gameRoom);
    gameRoomRepository.delete(roomId);

    assertThat(gameRoomRepository.findAll().size()).isEqualTo(0);
  }

  public User createUser(String nickname, String character, Role role) {
    return User.builder()
        .nickname(nickname)
        .character(character)
        .role(role)
        .build();
  }
}
