package com.api.liargame.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {
  String roomId;
  String userId;
  String nickname;
  String character;
}
