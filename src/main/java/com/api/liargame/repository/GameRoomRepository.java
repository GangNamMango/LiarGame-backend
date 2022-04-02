package com.api.liargame.repository;

import com.api.liargame.domain.GameRoom;
import java.util.List;

public interface GameRoomRepository {
  String save(GameRoom gameRoom);
  void delete(String roomId);
  GameRoom findById(String roomId);
  List<GameRoom> findAll();
}
