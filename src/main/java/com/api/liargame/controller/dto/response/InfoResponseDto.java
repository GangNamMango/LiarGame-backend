package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.Info;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InfoResponseDto {

  private String liarName;
  private String topic;
  private String word;

  public static InfoResponseDto of(Info info) {
    return InfoResponseDto.builder()
        .liarName(info.getLiar().getNickname())
        .topic(info.getTopic())
        .word(info.getWord())
        .build();
  }
}
