package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.Info;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfoResponseDto {

  private String liarId;
  private String topic;
  private String word;

  public static InfoResponseDto of(Info info) {
    return InfoResponseDto.builder()
        .liarId(info.getLiar().getId())
        .topic(info.getTopic())
        .word(info.getWord())
        .build();
  }
}
