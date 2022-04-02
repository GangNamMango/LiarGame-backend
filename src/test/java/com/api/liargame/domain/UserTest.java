package com.api.liargame.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.api.liargame.domain.User.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {
  @Test
  @DisplayName("유저 생성")
  void createUser() {
    //given
    String nickname = "test";
    String character = "character1";

    //when
    User user = User.builder()
        .nickname(nickname)
        .character(character)
        .role(Role.GUEST)
        .build();

    //then
    assertThat(user.getId()).isNotEmpty();
    assertThat(user.getNickname()).isEqualTo(nickname);
    assertThat(user.getCharacter()).isEqualTo(character);
  }

  @Test
  @DisplayName("role을 지정하지 않으면 유저 생성에 실패해야 한다.")
  void fail_when_not_role() {
    String nickname = "test";
    String character = "character1";

    assertThrows(IllegalStateException.class, () -> User.builder()
        .nickname(nickname)
        .character(character)
        .build());
  }
}
