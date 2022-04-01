package com.api.liargame.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EnterRequestDto {
  private String roomId;
  private UserRequestDto user;
}
