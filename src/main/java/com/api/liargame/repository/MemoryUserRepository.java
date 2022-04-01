package com.api.liargame.repository;

import com.api.liargame.domain.User;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryUserRepository implements UserRepository {

  private final static List<User> users = new LinkedList<>();

  @Override
  public String save(User user) {
    users.add(user);
    return user.getId();
  }

  @Override
  public void delete(String id) {
    User user = findById(id);

    if (user == null) return;

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
}
