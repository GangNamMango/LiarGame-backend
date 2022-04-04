package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class GameRoomDto {

  private final String roomId;
  private final String hostId;
  private final List<UserResponseDto> users;
  private final Setting settings;

  public GameRoomDto(GameRoom gameRoom) {
    Set<User> users = gameRoom.getUsers();

    this.roomId = gameRoom.getRoomId();
    this.hostId = gameRoom.getHost().getId();
    this.users = users.stream().map(UserResponseDto::new).collect(Collectors.toList());
    this.settings = gameRoom.getSettings();
  }
}
