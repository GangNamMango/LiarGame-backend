package com.api.liargame.repository;

import com.api.liargame.domain.GameRoom;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class MemoryGameRoomRepository implements GameRoomRepository{

  private final Map<String, GameRoom> gameRoomMemory = new HashMap<>();

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

  @Override
  public String createRoomId(){
    int length = 5;
    String str = "qwertyuiopasdfghjklzxcvbnm123456789";
    String roomId = "";
    Random random = new Random();
    for (int i =0; i < 5; i++) roomId += str.charAt( random.nextInt(str.length()));
    return roomId;
  }
}
