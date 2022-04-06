package com.api.liargame.domain;

import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.DuplicateUserNicknameException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameRoom {

  private final String roomId;
  private final Set<User> users = new HashSet<>();
  private User host;
  private Setting setting;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Builder
  public GameRoom(String roomId, User host, Setting setting) {
    if (host == null || setting == null | host.getRole() != Role.HOST) {
      throw new IllegalStateException();
    }
    this.roomId = roomId;
    this.host = host;
    this.users.add(host);
    this.setting = setting;
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

  public void setSetting(Setting setting) {
    this.setting = setting;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void update() {
    setUpdatedAt(LocalDateTime.now());
  }
}
