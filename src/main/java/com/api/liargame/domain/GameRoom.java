package com.api.liargame.domain;

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
  public GameRoom(User host, Setting settings) {
    if (host == null || settings == null) {
      throw new IllegalStateException();
    }
    this.roomId = UUID.randomUUID().toString(); // TODO : roomId 생성 자체 알고리즘으로 변경
    this.host = host;
    this.users.add(host);
    this.settings = settings;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void addUser(User user) {
    this.users.add(user);
  }

  public void deleteUser(User user) {
    this.users.remove(user);
  }

  public void changeHost(User user) {
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
