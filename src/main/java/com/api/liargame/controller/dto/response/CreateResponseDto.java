package com.api.liargame.controller.dto.response;

import lombok.Getter;

@Getter
public class CreateResponseDto {
  String userId;
  GameRoomResponseDto gameRoom;

  public CreateResponseDto(String userId, GameRoomResponseDto gameRoom) {
    this.userId = userId;
    this.gameRoom = gameRoom;
  }
}
