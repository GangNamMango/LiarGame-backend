package com.api.liargame.controller;

import com.api.liargame.controller.dto.request.ChoiceRequestDto;
import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.GameStartRequestDto;
import com.api.liargame.controller.dto.request.LeaveRequestDto;
import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.controller.dto.request.UpdateProfileRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.controller.dto.request.VoteRequestDto;
import com.api.liargame.controller.dto.response.CreateResponseDto;
import com.api.liargame.controller.dto.response.EnterResponseDto;
import com.api.liargame.controller.dto.response.GameResultResponseDto;
import com.api.liargame.controller.dto.response.GameRoomResponseDto;
import com.api.liargame.controller.dto.response.InfoResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.controller.dto.response.UserResponseDto;
import com.api.liargame.controller.dto.response.VoteResponseDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.GameStatus;
import com.api.liargame.domain.Info;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.service.GameRoomService;
import com.api.liargame.service.SettingService;
import java.util.List;
import java.util.stream.Collectors;
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
    GameRoom gameRoom = gameRoomService.getGameRoomOrFail(roomId);
    User leavedUser = gameRoomService.leave(gameRoom, userId);

    if (gameRoom.getGameStatus().equals(GameStatus.END)) {
      gameRoomService.processEndGame(gameRoom);

      GameResultResponseDto gameResultResponseDto = GameResultResponseDto.ofGameExit(gameRoom, leavedUser);

      ResponseDto<?> endResultResponse = ResponseDto.builder()
          .status(ResponseStatus.SUCCESS)
          .message("게임을 더 이상 진행할 수 없습니다.")
          .data(gameResultResponseDto)
          .build();

      webSocket.convertAndSend("/sub/game/result/" + roomId, endResultResponse);
      return endResultResponse;
    }

    List<UserResponseDto> userResponseList = gameRoom.getUsers()
        .stream()
        .map(UserResponseDto::new)
        .collect(Collectors.toList());

    ResponseDto<?> socketResponse = ResponseDto.<List<UserResponseDto>>builder()
        .status(ResponseStatus.SUCCESS)
        .message(leavedUser.getNickname() + "님이 게임을 나갔습니다.")
        .data(userResponseList)
        .build();

    webSocket.convertAndSend("/sub/game/leave/" + roomId, socketResponse);
    return socketResponse;
  }

  @MessageMapping("/setting")
  public ResponseDto<?> updateSetting(@Payload SettingRequestDto settingRequestDto) {
    String roomId = settingRequestDto.getRoomId();
    GameRoom gameRoom = gameRoomService.getGameRoomOrFail(roomId);
    try {
      Setting updatedSetting = settingService.updateSetting(gameRoom, settingRequestDto);

      ResponseDto<Setting> socketResponse = ResponseDto.<Setting>builder()
          .status(ResponseStatus.SUCCESS)
          .message("게임 설정이 변경되었습니다.")
          .data(updatedSetting)
          .build();

      webSocket.convertAndSend("/sub/game/setting/" + gameRoom.getRoomId(), socketResponse);
      return socketResponse;
    } catch (RuntimeException ex) {
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

      webSocket.convertAndSend("/sub/game/error/" + updateProfileRequestDto.getUserId(),
          failResponse);
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
      gameRoomService.gameCountdown(roomId, "/sub/game/countdown/" + roomId);
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

  @MessageMapping("/vote")
  public ResponseDto<?> vote(VoteRequestDto voteRequestDto) {
    String roomId = voteRequestDto.getRoomId();
    String userId = voteRequestDto.getUserId();
    String voteTo = voteRequestDto.getVoteTo();

    try {
      GameRoom gameRoom = gameRoomService.vote(roomId, userId, voteTo);

      VoteResponseDto voteResponseDto = VoteResponseDto.of(GameStatus.VOTE, gameRoom);

      ResponseDto<?> socketResponse = ResponseDto.builder()
          .status(ResponseStatus.SUCCESS)
          .message("투표 완료")
          .data(voteResponseDto)
          .build();

      webSocket.convertAndSend("/sub/game/vote/" + roomId, socketResponse);

      //모든 유저의 투표가 완료되었으면 게임 결과를 반환한다.
      if (gameRoomService.checkVoteComplete(roomId)) {
        GameResultResponseDto gameResultDto = gameRoomService.getGameResult(roomId, userId, null);
        ResponseDto<?> gameResultResponse = ResponseDto.builder()
            .status(ResponseStatus.SUCCESS)
            .message("모든 유저의 투표 완료")
            .data(gameResultDto)
            .build();

        webSocket.convertAndSend("/sub/game/result/" + roomId, gameResultResponse);
        return gameResultResponse;
      }
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

  @MessageMapping("/choice")
  public ResponseDto<?> choice(ChoiceRequestDto choiceDto) {
    String roomId = choiceDto.getRoomId();
    String userId = choiceDto.getUserId();

    try {
      GameResultResponseDto gameResultDto = gameRoomService.getGameResult(roomId, userId,
          choiceDto.getChoice());

      ResponseDto<?> socketResponse = ResponseDto.builder()
          .status(ResponseStatus.SUCCESS)
          .message("게임 결과")
          .data(gameResultDto)
          .build();

      webSocket.convertAndSend("/sub/game/result/" + roomId, socketResponse);
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
}
