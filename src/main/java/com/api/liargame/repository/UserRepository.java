package com.api.liargame.repository;

import com.api.liargame.domain.User;
import java.util.List;

public interface UserRepository {
  String save(User user);
  void delete(String id);
  User findById(String id);
  List<User> findAll();
}
