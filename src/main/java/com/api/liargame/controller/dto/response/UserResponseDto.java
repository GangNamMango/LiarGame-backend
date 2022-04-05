package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.User;
import java.util.Locale;
import lombok.Getter;

@Getter
public class UserResponseDto {
  private final String nickname;
  private final String role;
  private final String gameRole;
  private final String character;

  public UserResponseDto(User user) {
    this.nickname = user.getNickname();
    this.role = user.getRole().toString().toLowerCase(Locale.ROOT);
    this.gameRole = user.getGameRole().toString().toLowerCase(Locale.ROOT);
    this.character = user.getCharacter();
  }
}
