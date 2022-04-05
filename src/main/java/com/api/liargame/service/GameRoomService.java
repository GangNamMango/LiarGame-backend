package com.api.liargame.service;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.User;

public interface GameRoomService {

  GameRoom createRoom(UserRequestDto userRequestDto);

  User enter(EnterRequestDto enterRequestDto);

  GameRoom find(String roomId);

  User leave(String roomId, String userId);

  String createGameRoomId();

  String randomRoomId();
}
