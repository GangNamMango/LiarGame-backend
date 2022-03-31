package com.api.liargame.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public class User {
  public enum Role {
    HOST,
    GUEST
  }
  public enum GameRole {
    LIAR,
    MEMBER
  }

  private final String id;
  private String nickname;
  private Role role;
  private GameRole gameRole;
  private String character;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Builder
  public User(String nickname, Role role, GameRole gameRole, String character) {
    this.id = UUID.randomUUID().toString();
    this.nickname = nickname;
    this.role = role;
    this.gameRole = gameRole;
    this.character = character;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public void setGameRole(GameRole gameRole) {
    this.gameRole = gameRole;
  }

  public void setCharacter(String character) {
    this.character = character;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  private void update() {
    setUpdatedAt(LocalDateTime.now());
  }
}
