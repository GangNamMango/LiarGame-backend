package com.api.liargame.controller.dto.response;

import lombok.Getter;

@Getter
public class EnterResponseDto {
  String userId;
  GameRoomResponseDto gameRoom;

  public EnterResponseDto(String userId, GameRoomResponseDto gameRoom) {
    this.userId = userId;
    this.gameRoom = gameRoom;
  }
}
