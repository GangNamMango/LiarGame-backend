package com.api.liargame.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
public class User {
  private final String id;
  private String nickname;
  private String role;
  private String gameRole;
  private String character;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Builder
  public User(String nickname, String role, String gameRole, String character) {
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

  public void setRole(String role) {
    this.role = role;
  }

  public void setGameRole(String gameRole) {
    this.gameRole = gameRole;
  }

  public void setCharacter(String character) {
    this.character = character;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
