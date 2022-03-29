package com.api.liargame.controller.dto.request;

import lombok.Getter;

@Getter
public class EnterRequestDto {
  private String roomId;
  private UserRequestDto user;
}
