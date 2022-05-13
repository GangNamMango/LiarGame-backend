package com.api.liargame.repository;

import com.api.liargame.domain.GameRoom;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryGameRoomRepository implements GameRoomRepository{

  private final static Map<String, GameRoom> gameRoomMemory = new HashMap<>();

  @Override
  public String save(GameRoom gameRoom) {
    gameRoomMemory.put(gameRoom.getRoomId(), gameRoom);

    return gameRoom.getRoomId();
  }

  @Override
  public void delete(String roomId) {
    GameRoom gameRoom = findById(roomId);

    if (gameRoom == null) return;

    gameRoomMemory.remove(roomId);
  }

  @Override
  public GameRoom findById(String roomId) {
    return gameRoomMemory.get(roomId);
  }

  @Override
  public List<GameRoom> findAll() {
    return new ArrayList<>(gameRoomMemory.values());
  }

  public void clear() {
    gameRoomMemory.clear();
  }
}
