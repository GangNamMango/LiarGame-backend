package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.repository.UserRepository;
import com.api.liargame.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequiredArgsConstructor
@RequestMapping("/game")
@MessageMapping("/game")
public class GameRoomController {

  private final SimpMessagingTemplate webSocket;
  private final GameRoomService gameRoomService;
  private final UserRepository userRepository;

  @MessageMapping(value = "/enter")
  public void enter(EnterRequestDto enterRequestDto) {
    String roomId = enterRequestDto.getRoomId();

  }

  @PostMapping("/room")
  @ResponseBody
  public GameRoom createRoom(@RequestBody UserRequestDto userRequestDto) {
    System.out.println(userRequestDto.toString());    
    return gameRoomService.createdRoom(userRequestDto);

  }
}
