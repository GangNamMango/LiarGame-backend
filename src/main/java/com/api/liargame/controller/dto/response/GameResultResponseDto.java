package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.User.GameRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Locale;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class GameResultResponseDto {

  private final String gameStatus;
  private final String winner;
  private final String liarName;
  private final String liarAnswer;
  private final String word;
  private final int voteCount;

  @Builder
  public GameResultResponseDto(String gameStatus, String winner, String liarName,
      String liarAnswer, String word, int voteCount) {
    this.gameStatus = gameStatus;
    this.winner = winner;
    this.liarName = liarName;
    this.liarAnswer = liarAnswer;
    this.word = word;
    this.voteCount = voteCount;
  }

  public static GameResultResponseDto ofMemberWin(GameStatus gameStatus, GameRole winner,
      String liarName, String liarAnswer, String word) {
    return GameResultResponseDto.builder()
        .gameStatus(gameStatus.name())
        .winner(winner.toString().toLowerCase(Locale.ROOT))
        .liarName(liarName)
        .liarAnswer(liarAnswer)
        .word(word)
        .build();
  }

  public static GameResultResponseDto ofLiarWin(GameStatus gameStatus, GameRole winner, String liarName, String word) {
    return GameResultResponseDto.builder()
        .gameStatus(gameStatus.name())
        .winner(winner.toString().toLowerCase(Locale.ROOT))
        .liarName(liarName)
        .word(word)
        .build();
  }

  public static GameResultResponseDto ofLiarChoice(GameStatus gameStatus, String liarName, int voteCount) {
    return GameResultResponseDto.builder()
        .gameStatus(gameStatus.name())
        .liarName(liarName)
        .voteCount(voteCount)
        .build();
  }
}
