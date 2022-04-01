package com.api.liargame.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.repository.GameRoomRepository;
import com.api.liargame.service.GameRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;


//TODO : Mock 활용한 테스트로 변경 / 통합테스트는 따로 빼기
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
class GameRoomControllerTest {

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private GameRoomRepository gameRoomRepository;


  private final static ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("방 입장 api 테스트")
  void enter() throws Exception {
    GameRoom gameRoom = createGameRoom();

    String roomId = gameRoomRepository.save(gameRoom);
    UserRequestDto userRequestDto = new UserRequestDto("user2", "ch2");

    EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);
    String body = objectMapper.writeValueAsString(enterRequestDto);

    RequestEntity request = RequestEntity.post("/game/enter").accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);

    Map<String, Object> response = testRestTemplate.exchange(request, Map.class).getBody();
    Map<String, Object> data = (Map)response.get("data");

    assertThat(response.get("status")).isEqualTo(ResponseStatus.SUCCESS);
    assertThat(data.get("roomId")).isEqualTo(roomId);
  }

  @Test
  @DisplayName("방 존재하지 않으면 fail 발생해야함")
  void fail_when_invalid_roomId() throws Exception {
    UserRequestDto userRequestDto = new UserRequestDto("user2", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto("test", userRequestDto);

    String body = objectMapper.writeValueAsString(enterRequestDto);

    RequestEntity request = RequestEntity.post("/game/enter").accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);

    Map<String, Object> response = testRestTemplate.exchange(request, Map.class).getBody();

    assertThat(response.get("status")).isEqualTo(ResponseStatus.FAILURE);
  }

  @Test
  @DisplayName("중복된 유저는 생성에 실패해야함.")
  void fail_when_duplicate_nickname() throws Exception {
    GameRoom gameRoom = createGameRoom();

    String roomId = gameRoomRepository.save(gameRoom);
    UserRequestDto userRequestDto = new UserRequestDto("user1", "ch2");
    EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);

    String body = objectMapper.writeValueAsString(enterRequestDto);

    RequestEntity request = RequestEntity.post("/game/enter").accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);

    Map<String, Object> response = testRestTemplate.exchange(request, Map.class).getBody();

    assertThat(response.get("status")).isEqualTo(ResponseStatus.FAILURE);
    assertThat(response.get("message")).isEqualTo("Duplicate User Nickname");
  }

  private GameRoom createGameRoom() {
    User user = User.builder()
        .nickname("user1")
        .character("ch1")
        .role(Role.HOST)
        .build();
    Setting setting = new Setting();
    GameRoom gameRoom = GameRoom.builder()
        .host(user)
        .settings(setting)
        .build();

    return gameRoom;
  }
}
