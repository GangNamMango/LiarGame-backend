package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.GameStatus;
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
  private final List<String> notCompleted;

  @Builder
  public VoteResponseDto(String gameStatus, Integer maxVoteCount, Integer currentVoteCount,
      List<String> completed, List<String> noteCompleted) {
    this.gameStatus = gameStatus;
    this.maxVoteCount = maxVoteCount;
    this.currentVoteCount = currentVoteCount;
    this.completed = completed;
    this.notCompleted = noteCompleted;
  }

  public static VoteResponseDto of(GameStatus gameStatus, GameRoom gameRoom) {
    int maxVoteCount = gameRoom.getUsers().size();
    int currentVoteCount = gameRoom.getVoteCompleteCount();
    List<String> completedUsers = new ArrayList<>();
    List<String> notCompletedUsers = new ArrayList<>();

    gameRoom.getUsers().forEach(u -> {
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
