package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.GameRoomDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.controller.dto.response.UserResponseDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.service.GameRoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/game")
@MessageMapping("/game")
public class GameRoomController {

  private final SimpMessagingTemplate webSocket;
  private final GameRoomService gameRoomService;

  @PostMapping("/enter")
  public ResponseDto<GameRoomDto> enter(@RequestBody EnterRequestDto enterRequestDto) {
    GameRoom gameRoom = gameRoomService.enter(enterRequestDto);
    GameRoomDto gameRoomResponse = new GameRoomDto(gameRoom);

    String enterUserNickname = enterRequestDto.getUser().getNickname();

    ResponseDto<GameRoomDto> httpResponse = ResponseDto.<GameRoomDto>builder()
        .status(ResponseStatus.SUCCESS)
        .message("입장에 성공했습니다.")
        .data(gameRoomResponse)
        .build();

    ResponseDto<?> socketResponse = ResponseDto.<List<UserResponseDto>>builder()
        .status(ResponseStatus.SUCCESS)
        .message(enterUserNickname + "님이 입장하셨습니다.")
        .data(gameRoomResponse.getUsers())
        .build();

    webSocket.convertAndSend("/sub/game/" + gameRoom.getRoomId(), socketResponse);
    return httpResponse;
  }

  @PostMapping("/room")
  public ResponseDto<GameRoomDto> createRoom(@RequestBody UserRequestDto userRequestDto) {
    GameRoom gameRoom = gameRoomService.createRoom(userRequestDto);
    GameRoomDto gameRoomResponse = new GameRoomDto(gameRoom);

    ResponseDto<GameRoomDto> httpResponse = ResponseDto.<GameRoomDto>builder()
        .status(ResponseStatus.SUCCESS)
        .message("게임 방이 생성되었습니다.")
        .data(gameRoomResponse)
        .build();

    return httpResponse;
  }
}
