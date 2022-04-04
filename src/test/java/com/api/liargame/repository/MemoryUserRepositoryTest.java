package com.api.liargame.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemoryUserRepositoryTest {

  UserRepository userRepository = new MemoryUserRepository();



  @BeforeEach
  void beforeEach() {
    userRepository.clear();
  }

  @Test
  @DisplayName("유저를 저장할 수 있어야 한다.")
  void save() {
    User user1 = User.builder()
        .nickname("user1")
        .character("c1")
        .role(Role.HOST)
        .build();

    User user2 = User.builder()
        .nickname("user2")
        .character("c2")
        .role(Role.GUEST)
        .build();

    String savedId = userRepository.save(user1);
    userRepository.save(user2);
    User savedUser = userRepository.findById(savedId);
    List<User> users = userRepository.findAll();

    assertThat(savedUser.getNickname()).isEqualTo(user1.getNickname());
    assertThat(savedId).isEqualTo(user1.getId());
    assertThat(users.size()).isEqualTo(2);
  }

  @Test
  @DisplayName("유저를 삭제할 수 있어야 한다.")
  public void delete() {
    User user1 = User.builder()
        .nickname("user1")
        .character("c1")
        .role(Role.HOST)
        .build();

    userRepository.save(user1);
    userRepository.delete(user1.getId());
    User foundUser = userRepository.findById(user1.getId());
    List<User> users = userRepository.findAll();

    assertThat(foundUser).isNull();
    assertThat(users.size()).isEqualTo(0);
  }

}
