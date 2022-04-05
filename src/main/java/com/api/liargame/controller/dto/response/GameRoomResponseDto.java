package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class GameRoomResponseDto {

  private final String roomId;
  private final List<UserResponseDto> users;
  private final Setting settings;

  public GameRoomResponseDto(GameRoom gameRoom) {
    Set<User> users = gameRoom.getUsers();

    this.roomId = gameRoom.getRoomId();
    this.users = users.stream().map(UserResponseDto::new).collect(Collectors.toList());
    this.settings = gameRoom.getSettings();
  }
}
