package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import java.util.List;
import lombok.Getter;

@Getter
public class GameRoomDto {
  private String roomId;
  private List<UserResponseDto> users;
  private UserResponseDto host;
  private Setting settings;

  public GameRoomDto(GameRoom gameRoom) {}
}
