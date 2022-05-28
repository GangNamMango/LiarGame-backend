package com.api.liargame.repository;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.global.SlackLogger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemoryGameRoomRepository implements GameRoomRepository {

  private final static Map<String, GameRoom> gameRoomMemory = new HashMap<>();
  private final SlackLogger slackLogger;


  @Override
  public String save(GameRoom gameRoom) {
    gameRoomMemory.put(gameRoom.getRoomId(), gameRoom);

    log.info("[✅방 생성] 방이 생성되었습니다. (CODE : {}, HOST : {})", gameRoom.getRoomId(),
        gameRoom.getHost().getNickname());

    slackLogger.send(String.format("[✅방 생성] 방이 생성되었습니다. (방 번호 : %s, 방장 닉네임 : %s)",
        gameRoom.getRoomId(), gameRoom.getHost().getNickname()));

    printGameRoom();

    return gameRoom.getRoomId();
  }

  @Override
  public void delete(String roomId) {
    GameRoom gameRoom = findById(roomId);

    if (gameRoom == null) {
      return;
    }

    gameRoomMemory.remove(roomId);

    log.info("[❎방 삭제] 방이 삭제되었습니다. (CODE : {}, HOST : {})", gameRoom.getRoomId(),
        gameRoom.getHost().getNickname());

    slackLogger.send(String.format("[❎방 삭제] 방이 삭제되었습니다. (방 번호 : %s, 방장 닉네임 : %s)",
        gameRoom.getRoomId(), gameRoom.getHost().getNickname()));

    printGameRoom();
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

  private void printGameRoom() {
    String currentDate = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    log.info("🛑현재 방 정보 [{}]🛑", currentDate);
    log.info("총 방의 개수 : {}", gameRoomMemory.size());
    for (GameRoom room : gameRoomMemory.values()) {
      log.info("----------------------------------");
      log.info("방 번호 : {} | 방장 닉네임 : {}", room.getRoomId(), room.getHost().getNickname());
      log.info("게임 상태 : {}, | 접속 유저 수 : {}", room.getGameStatus(), room.getUserCount());
      log.info("----------------------------------");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("🛑현재 방 정보 [").append(currentDate).append("]🛑\n");
    sb.append("총 방의 개수 : ").append(gameRoomMemory.size()).append("\n");
    for (GameRoom room : gameRoomMemory.values()) {
      sb.append("----------------------------------").append("\n");
      sb.append("방 번호 : ").append(room.getRoomId()).append(" | ").append("방장 닉네임 : ")
          .append(room.getHost().getNickname()).append("\n");
      sb.append("게임 상태 : ").append(room.getGameStatus()).append(" | ").append("접속 유저 수 : ")
          .append(room.getUserCount()).append("\n");
      sb.append("----------------------------------").append("\n");
    }

    slackLogger.send(sb.toString());
  }
}
