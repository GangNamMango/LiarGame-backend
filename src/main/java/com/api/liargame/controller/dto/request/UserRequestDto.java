package com.api.liargame.controller.dto.request;

import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

  private String nickname;
  private String character;

  public User toEntity() {
    return User.builder()
        .nickname(nickname)
        .role(Role.GUEST)
        .character(character)
        .build();
  }
}
