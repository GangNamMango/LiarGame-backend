package com.api.liargame.service;

import com.api.liargame.controller.dto.request.ChoiceRequestDto;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.CounterResponseDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.User;

public interface GameRoomService {

  GameRoom createRoom(UserRequestDto userRequestDto);

  User enter(EnterRequestDto enterRequestDto);

  GameRoom find(String roomId);

  User leave(String roomId, String userId);

  User updateUserProfile(UpdateProfileRequestDto updateProfileRequestDto);

  Info createGameInfo(String roomId, String userId);

  String createGameRoomId();

  String randomRoomId();

  void gameCountdown(String roomId);

  boolean isSame(GameRoom gameRoom, String word);
  
  void isLiar(GameRoom roomId, String liarId);

  boolean checkAnswer(ChoiceRequestDto choiceRequestDto);

}
