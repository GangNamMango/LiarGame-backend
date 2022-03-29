package com.api.liargame.controller.dto.response;

import com.api.liargame.domain.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
  private String id;
  private String nickname;
  private String role;
  private String gameRole;
  private String character;

  public UserResponseDto(User user) {}
}
