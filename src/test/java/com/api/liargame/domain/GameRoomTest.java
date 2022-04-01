package com.api.liargame.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.DuplicateUserNicknameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameRoomTest {
  @Test
  @DisplayName("방 생성")
  void create() {
    //given
    User user = User.builder()
        .nickname("user1")
        .character("c1")
        .role(Role.HOST)
        .build();
    User user2 = User.builder()
        .nickname("user2")
        .character("c2")
        .role(Role.GUEST)
        .build();
    Setting setting = new Setting();

    //when
    GameRoom gameRoom = GameRoom.builder()
        .host(user)
        .settings(setting)
        .build();
    gameRoom.addUser(user2);

    //then
    assertThat(gameRoom.getRoomId()).isNotEmpty();
    assertThat(gameRoom.getUsers().size()).isEqualTo(2);
    assertThat(gameRoom.getHost()).isSameAs(user);
  }

  @Test
  @DisplayName("참가 유저를 삭제할 수 있어야 한다.")
  public void deleteUser() {
    User user = User.builder()
        .nickname("user1")
        .character("c1")
        .role(Role.HOST)
        .build();
    User user2 = User.builder()
        .nickname("user2")
        .character("c2")
        .role(Role.GUEST)
        .build();
    Setting setting = new Setting();
    GameRoom gameRoom = GameRoom.builder()
        .host(user)
        .settings(setting)
        .build();
    gameRoom.addUser(user2);

    //when
    gameRoom.deleteUser(user2);

    //then
    assertThat(gameRoom.getUsers().size()).isEqualTo(1);
  }

  @Test
  @DisplayName("호스트를 변경할 수 있어야 한다.")
  public void changeHost() {
    User originalHost = User.builder()
        .nickname("user1")
        .character("c1")
        .role(Role.HOST)
        .build();
    User changeHost = User.builder()
        .nickname("user2")
        .character("c2")
        .role(Role.GUEST)
        .build();
    Setting setting = new Setting();
    GameRoom gameRoom = GameRoom.builder()
        .host(originalHost)
        .settings(setting)
        .build();

    gameRoom.changeHost(changeHost);

    assertThat(gameRoom.getHost()).isSameAs(changeHost);
    assertThat(changeHost.getRole()).isEqualTo(Role.HOST);
  }

  @Test
  @DisplayName("호스트와 설정이 없으면 방 생성에 실패해야한다.")
  void create_fail() {
    assertThrows(IllegalStateException.class, () -> {
      GameRoom gameRoom = GameRoom.builder()
          .build();
    });
  }

  @Test
  @DisplayName("중복된 닉네임의 유저는 추가할 수 없다.")
  void duplicate_user_nickname() {
    User user = User.builder()
        .nickname("user1")
        .character("c1")
        .role(Role.HOST)
        .build();

    User user2 = User.builder()
        .nickname("user1")
        .character("c2")
        .role(Role.GUEST)
        .build();

    Setting setting = new Setting();
    GameRoom gameRoom = GameRoom.builder()
        .host(user)
        .settings(setting)
        .build();

    assertThrows(DuplicateUserNicknameException.class, () -> gameRoom.addUser(user2));
  }
}
