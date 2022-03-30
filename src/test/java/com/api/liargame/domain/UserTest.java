package com.api.liargame.domain;

import static org.assertj.core.api.Assertions.*;
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
        .build();

    //then
    assertThat(user.getId()).isNotEmpty();
    assertThat(user.getNickname()).isEqualTo(nickname);
    assertThat(user.getCharacter()).isEqualTo(character);
  }
}
