package com.api.liargame.domain;

import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.DuplicateUserNicknameException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameRoom {

  private final String roomId;
  private final Set<User> users = new HashSet<>();
  private User host;
  private Setting settings;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Builder
  public GameRoom(String roomId, User host, Setting settings) {
    if (host == null || settings == null) {
      throw new IllegalStateException();
    }
    this.roomId = roomId;//UUID.randomUUID().toString(); // TODO : roomId 생성 자체 알고리즘으로 변경
    this.host = host;
    this.users.add(host);
    this.settings = settings;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void addUser(User user) {
    String nickname = user.getNickname();

    boolean isDuplicated = users
        .stream()
        .anyMatch(u -> u.getNickname().equals(nickname));

    if (isDuplicated) {
      throw new DuplicateUserNicknameException();
    }

    this.users.add(user);
  }

  public void deleteUser(User user) {
    this.users.remove(user);
  }

  public void changeHost(User user) {
    user.setRole(Role.HOST);

    this.host = user;
  }

  public void setSettings(Setting settings) {
    this.settings = settings;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void update() {
    setUpdatedAt(LocalDateTime.now());
  }
}
