package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.response.GameRoomDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.controller.dto.response.UserResponseDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.exception.SettingPermissionException;
import com.api.liargame.service.GameRoomService;
import com.api.liargame.service.SettingService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException.BadRequest;


@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/game")
@MessageMapping("/game")
public class GameRoomController {

  private final SimpMessagingTemplate webSocket;
  private final GameRoomService gameRoomService;
  private final SettingService settingService;
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
    webSocket.convertAndSend("/sub/game/enter/" + gameRoom.getRoomId(), socketResponse);
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

  @PostMapping("/room/setting/{roomId}")
  public ResponseDto<Setting> updatSetting(@PathVariable String roomId,  @RequestBody SettingRequestDto settingDto) {
    GameRoom gameRoom = gameRoomService.findRoom(roomId);
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
