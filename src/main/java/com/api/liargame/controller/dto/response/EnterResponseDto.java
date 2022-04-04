package com.api.liargame.controller.dto.response;

import lombok.Getter;

@Getter
public class EnterResponseDto {
  String userId;
  GameRoomDto gameRoom;

  public EnterResponseDto(String userId, GameRoomDto gameRoom) {
    this.userId = userId;
    this.gameRoom = gameRoom;
  }
}
