package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.LeaveRequestDto;
import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.CreateResponseDto;
import com.api.liargame.controller.dto.response.EnterResponseDto;
import com.api.liargame.controller.dto.response.GameRoomResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.controller.dto.response.UserResponseDto;
import com.api.liargame.domain.GameRoom;
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
import org.springframework.web.bind.annotation.PathVariable;
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
  public void leave(@Payload LeaveRequestDto leaveRequestDto) {
    String roomId = leaveRequestDto.getRoomId();
    String userId = leaveRequestDto.getUserId();
    User leavedUser = gameRoomService.leave(roomId, userId);
    GameRoom gameRoom = gameRoomService.find(roomId);

    if (gameRoom == null)
      return;

    GameRoomResponseDto gameRoomResponse = new GameRoomResponseDto(gameRoom);

    ResponseDto<?> socketResponse = ResponseDto.<GameRoomResponseDto>builder()
        .status(ResponseStatus.SUCCESS)
        .message(leavedUser.getNickname() + "님이 대기실을 나갔습니다.")
        .data(gameRoomResponse)
        .build();

    webSocket.convertAndSend("/sub/game/leave/" + roomId, socketResponse);
  }

  @PostMapping("/room/setting/{roomId}")
  public ResponseDto<Setting> updatSetting(@PathVariable String roomId,  @RequestBody SettingRequestDto settingDto) {
    GameRoom gameRoom = gameRoomService.find(roomId);
    settingService.checkPermission(gameRoom, settingDto.getId());
    Setting newSetting = settingDto.getSetting();
    Setting updatedSetting = settingService.updateSetting(gameRoom, newSetting);
    ResponseDto<Setting> httpResponse = ResponseDto.<Setting>builder()
      .status(ResponseStatus.SUCCESS)
      .message("게임 설정이 변경되었습니다.")
      .data(updatedSetting)
      .build();

    webSocket.convertAndSend("/sub/game/setting/" + gameRoom.getRoomId(), updatedSetting);
    return httpResponse;
  }
}
