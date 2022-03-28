package com.api.liargame.controller;

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
  public void enter() { //TODO : DTO 생성 필요
    System.out.println("enter room socket test");
  }

  @PostMapping("room")
  public void createRoom() {
    System.out.println("create room api test");
  }
}
