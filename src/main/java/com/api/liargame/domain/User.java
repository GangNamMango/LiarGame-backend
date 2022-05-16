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
  private boolean vote;
  private int voteCount;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Builder
  public User(String nickname, Role role, String character) {
    if (role == null) {
      throw new IllegalStateException("Not exist role");
    }
    this.id = UUID.randomUUID().toString();
    this.nickname = nickname;
    this.role = role;
    this.gameRole = GameRole.MEMBER;
    this.character = character;
    this.vote = false;
    this.voteCount = 0;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void setNickname(String nickname) {
    if (nickname == null) return;

    this.nickname = nickname;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public void setGameRole(GameRole gameRole) {
    this.gameRole = gameRole;
  }

  public void setCharacter(String character) {
    if(character == null) return;

    this.character = character;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setVote(boolean vote) {
    this.vote = vote;
  }

  public void update() {
    setUpdatedAt(LocalDateTime.now());
  }

  public void addVoteCount() {
    this.voteCount += 1;
  }

  public boolean isLiar() {
    return this.gameRole.equals(GameRole.LIAR);
  }
}
