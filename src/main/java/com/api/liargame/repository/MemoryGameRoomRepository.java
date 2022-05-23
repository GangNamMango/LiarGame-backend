package com.api.liargame.repository;

import com.api.liargame.domain.GameRoom;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MemoryGameRoomRepository implements GameRoomRepository {

  private final static Map<String, GameRoom> gameRoomMemory = new HashMap<>();

  @Override
  public String save(GameRoom gameRoom) {
    gameRoomMemory.put(gameRoom.getRoomId(), gameRoom);

    log.info("[✅방 생성] 방이 생성되었습니다. (CODE : {}, HOST : {})", gameRoom.getRoomId(),
        gameRoom.getHost().getNickname());
    return gameRoom.getRoomId();
  }

  @Override
  public void delete(String roomId) {
    GameRoom gameRoom = findById(roomId);

    if (gameRoom == null) {
      return;
    }

    log.info("[❎방 삭제] 방이 삭제되었습니다. (CODE : {}, HOST : {})", gameRoom.getRoomId(),
        gameRoom.getHost().getNickname());

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
