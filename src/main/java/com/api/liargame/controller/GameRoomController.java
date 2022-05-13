package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.ChoiceRequestDto;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.GameStartRequestDto;
import com.api.liargame.controller.dto.request.LeaveRequestDto;
import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.CreateResponseDto;
import com.api.liargame.controller.dto.response.EnterResponseDto;
import com.api.liargame.controller.dto.response.GameRoomResponseDto;
import com.api.liargame.controller.dto.response.InfoResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.controller.dto.response.UserResponseDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.service.GameRoomService;
import com.api.liargame.service.SettingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@CrossOrigin("*")
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/game")
@MessageMapping("/game")
public class GameRoomController {

  private final SimpMessagingTemplate webSocket;
  private final GameRoomService gameRoomService;
  private final SettingService settingService;

  @PostMapping("/enter")
  public ResponseDto<EnterResponseDto> enter(@RequestBody EnterRequestDto enterRequestDto) {
    User enteredUser = gameRoomService.enter(enterRequestDto);
    GameRoom gameRoom = gameRoomService.find(enterRequestDto.getRoomId());
    GameRoomResponseDto gameRoomDto = new GameRoomResponseDto(gameRoom);

    EnterResponseDto enterResponseDto = new EnterResponseDto(enteredUser.getId(), gameRoomDto);

    ResponseDto<EnterResponseDto> httpResponse = ResponseDto.<EnterResponseDto>builder()
        .status(ResponseStatus.SUCCESS)
        .message("입장에 성공했습니다.")
        .data(enterResponseDto)
        .build();

    ResponseDto<?> socketResponse = ResponseDto.<List<UserResponseDto>>builder()
        .status(ResponseStatus.SUCCESS)
        .message(enteredUser.getNickname() + "님이 입장하셨습니다.")
        .data(gameRoomDto.getUsers())
        .build();

    webSocket.convertAndSend("/sub/game/enter/" + gameRoom.getRoomId(), socketResponse);

    return httpResponse;
  }

  @PostMapping("/room")
  public ResponseDto<CreateResponseDto> createRoom(@RequestBody UserRequestDto userRequestDto) {
    GameRoom gameRoom = gameRoomService.createRoom(userRequestDto);
    GameRoomResponseDto gameRoomResponse = new GameRoomResponseDto(gameRoom);

    CreateResponseDto createResponseDto = new CreateResponseDto(gameRoom.getHost().getId(),
        gameRoomResponse);

    ResponseDto<CreateResponseDto> httpResponse = ResponseDto.<CreateResponseDto>builder()
        .status(ResponseStatus.SUCCESS)
        .message("게임 방이 생성되었습니다.")
        .data(createResponseDto)
        .build();
    return httpResponse;
  }

  @MessageMapping("/leave")
  public ResponseDto<?> leave(@Payload LeaveRequestDto leaveRequestDto) {
    String roomId = leaveRequestDto.getRoomId();
    String userId = leaveRequestDto.getUserId();
    User leavedUser = gameRoomService.leave(roomId, userId);
    GameRoom gameRoom = gameRoomService.find(roomId);

    if (gameRoom == null)
      return new ResponseDto<>(ResponseStatus.FAILURE, "방이 존재하지 않습니다", null);

    GameRoomResponseDto gameRoomResponse = new GameRoomResponseDto(gameRoom);

    ResponseDto<?> socketResponse = ResponseDto.<List<UserResponseDto>>builder()
        .status(ResponseStatus.SUCCESS)
        .message(leavedUser.getNickname() + "님이 대기실을 나갔습니다.")
        .data(gameRoomResponse.getUsers())
        .build();

    webSocket.convertAndSend("/sub/game/leave/" + roomId, socketResponse);

    return socketResponse;
  }

  @MessageMapping("/setting")
  public ResponseDto<?> updateSetting(@Payload SettingRequestDto settingRequestDto) {
    String roomId = settingRequestDto.getRoomId();
    GameRoom gameRoom = gameRoomService.find(roomId);
    try {
      Setting updatedSetting = settingService.updateSetting(gameRoom, settingRequestDto);

      ResponseDto<Setting> socketResponse = ResponseDto.<Setting>builder()
          .status(ResponseStatus.SUCCESS)
          .message("게임 설정이 변경되었습니다.")
          .data(updatedSetting)
          .build();

      webSocket.convertAndSend("/sub/game/setting/" + gameRoom.getRoomId(), socketResponse);
      return socketResponse;
    } catch(RuntimeException ex) {
      ResponseDto<?> failResponse = ResponseDto.builder()
          .status(ResponseStatus.FAILURE)
          .message(ex.getMessage())
          .build();

      webSocket.convertAndSend("/sub/game/error/" + settingRequestDto.getUserId(), failResponse);
      return failResponse;
    }
  }

  @MessageMapping("/profile")
  public ResponseDto<?> updateUserProfile(UpdateProfileRequestDto updateProfileRequestDto) {
    try {
      String roomId = updateProfileRequestDto.getRoomId();
      gameRoomService.updateUserProfile(updateProfileRequestDto);
      GameRoom updatedGameRoom = gameRoomService.find(roomId);
      GameRoomResponseDto gameRoomResponseDto = new GameRoomResponseDto(updatedGameRoom);

      ResponseDto<?> socketResponse = ResponseDto.<List<UserResponseDto>>builder()
          .status(ResponseStatus.SUCCESS)
          .message("프로필이 변경되었습니다.")
          .data(gameRoomResponseDto.getUsers())
          .build();

      webSocket.convertAndSend("/sub/game/profile/" + roomId, socketResponse);
      return socketResponse;
    } catch (RuntimeException ex) {
      ResponseDto<?> failResponse = ResponseDto.builder()
          .status(ResponseStatus.FAILURE)
          .message(ex.getMessage())
          .build();

      webSocket.convertAndSend("/sub/game/error/" + updateProfileRequestDto.getUserId(), failResponse);
      return failResponse;
    }
  }

  @MessageMapping("/start")
  public ResponseDto<?> start(GameStartRequestDto gameStartRequestDto) {
    String roomId = gameStartRequestDto.getRoomId();
    String userId = gameStartRequestDto.getUserId();

    try {
      Info gameInfo = gameRoomService.createGameInfo(roomId, userId);
      InfoResponseDto infoResponseDto = InfoResponseDto.of(gameInfo);

      ResponseDto<?> socketResponse = ResponseDto.<InfoResponseDto>builder()
          .status(ResponseStatus.SUCCESS)
          .message("게임 시작")
          .data(infoResponseDto)
          .build();

      webSocket.convertAndSend("/sub/game/start/" + roomId, socketResponse);
      return socketResponse;
    } catch (RuntimeException ex) {
      ResponseDto<?> failResponse = ResponseDto.builder()
          .status(ResponseStatus.FAILURE)
          .message(ex.getMessage())
          .build();

      webSocket.convertAndSend("/sub/game/error/" + userId, failResponse);
      return failResponse;
    }
  }
  
  @MessageMapping("/pub/game/choice")
  public ResponseDto<?> choice(ChoiceRequestDto choiceDto) {
    GameRoom gameRoom = gameRoomService.find(choiceDto.getRoomId());
    String roomId = gameRoom.getRoomId();
    String liarId = choiceDto.getUserId();
    try{
      gameRoomService.isLiar(roomId, liarId);
      gameRoomService.isSame(roomId, choiceDto.getChoice());  
      ResponseDto<?> socketResponse = ResponseDto.builder()
      .status(ResponseStatus.SUCCESS)
      .message("맞았습니다")
      .data("단어 맞춤")
          .build();
      /*
        TODO
          결과를 어떤식으로 보내줘야 할지 
        webSocket.convertAndSend("/sub/game/result/" + userId, socketResponse);
      
      */
      return socketResponse;
    } catch (RuntimeException ex) {
      ResponseDto<?> failResponse = ResponseDto.builder()
      .status(ResponseStatus.FAILURE)
      .message(ex.getMessage())
      .build();
       /*
        TODO
          결과를 어떤식으로 보내줘야 할지 
        webSocket.convertAndSend("/sub/game/error/" + userId, failResponse);
      
      */
      return failResponse;
    }
  }
}
