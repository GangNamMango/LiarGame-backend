package com.api.liargame.service;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;

public interface GameRoomService {

  GameRoom createdRoom(UserRequestDto userRequestDto);

  String createGameRoomId();

  GameRoom enter(EnterRequestDto enterRequestDto);
}
