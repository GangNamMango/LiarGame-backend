package com.api.liargame.service;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;

public interface GameRoomService {

  GameRoom createRoom(UserRequestDto userRequestDto);


  GameRoom enter(EnterRequestDto enterRequestDto);

  String createGameRoomId();

  String randomRoomId();

  GameRoom findRoom(String roomId);
}
