package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/game")
@MessageMapping("/game")
public class GameRoomController {

  private final SimpMessagingTemplate webSocket;

  @MessageMapping(value = "/enter")
  public void enter(EnterRequestDto enterRequestDto) {
    String roomId = enterRequestDto.getRoomId();
    //roomId의 GameRoom이 존재하는지 확인
    //Room이 존재하면 /sub/game/:roomId로 ResponseDto<GameRoomDto> return
  }

  @PostMapping("/room")
  public void createRoom() {
    System.out.println("create room api test");
  }
}
