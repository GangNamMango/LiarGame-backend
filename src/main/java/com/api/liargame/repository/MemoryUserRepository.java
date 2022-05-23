package com.api.liargame.repository;

import com.api.liargame.domain.User;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MemoryUserRepository implements UserRepository {

  private final static List<User> users = new LinkedList<>();

  @Override
  public String save(User user) {
    users.add(user);

    log.info("[✅유저 생성] 유저가 레포지토리에 추가되었습니다. (ID : {}, NAME : {})", user.getId(),
        user.getNickname());

    return user.getId();
  }

  @Override
  public void delete(String id) {
    User user = findById(id);

    if (user == null) {
      return;
    }

    log.info("[❎유저 삭제] 유저가 레포지토리에서 삭제되었습니다. (ID : {}, NAME : {})", user.getId(),
        user.getNickname());

    users.remove(user);
  }

  @Override
  public User findById(String id) {
    return users.stream()
        .filter(u -> u.getId().equals(id))
        .findAny()
        .orElse(null);
  }

  @Override
  public List<User> findAll() {
    return users;
  }

  @Override
  public void clear() {
    users.clear();
  }
}
