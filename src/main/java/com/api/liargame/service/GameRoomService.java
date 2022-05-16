package com.api.liargame.service;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.GameResultResponseDto;
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

  void gameCountdown(String roomId, String event);

  GameRoom vote(String roomId, String userId, String voteTo);

  boolean checkVoteComplete(String roomId);

  GameResultResponseDto getGameResult(String roomId, String userId, String choice);

  boolean checkAnswer(String roomId, String userId, String choice);
}
