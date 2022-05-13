package com.api.liargame.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequestDto {

  private String userId;
  private String roomId;
  private String voteTo;
}
