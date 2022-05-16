package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VoteResponseDto {

  private final String gameStatus;
  private final Integer maxVoteCount;
  private final Integer currentVoteCount;
  private final List<String> completed;
  private final List<String> noteCompleted;

  @Builder
  public VoteResponseDto(String gameStatus, Integer maxVoteCount, Integer currentVoteCount,
      List<String> completed, List<String> noteCompleted) {
    this.gameStatus = gameStatus;
    this.maxVoteCount = maxVoteCount;
    this.currentVoteCount = currentVoteCount;
    this.completed = completed;
    this.noteCompleted = noteCompleted;
  }

  public static VoteResponseDto of(GameStatus gameStatus, List<User> users) {
    int maxVoteCount = users.size();
    int currentVoteCount = (int) users.stream()
        .filter(User::isVote)
        .count();
    List<String> completedUsers = new ArrayList<>();
    List<String> notCompletedUsers = new ArrayList<>();

    users.forEach(u -> {
          if (u.isVote()) {
            completedUsers.add(u.getNickname());
          } else {
            notCompletedUsers.add(u.getNickname());
          }
        });

    return VoteResponseDto.builder()
        .gameStatus(gameStatus.name())
        .maxVoteCount(maxVoteCount)
        .currentVoteCount(currentVoteCount)
        .completed(completedUsers)
        .noteCompleted(notCompletedUsers)
        .build();
  }
}
