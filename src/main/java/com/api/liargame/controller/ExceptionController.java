package com.api.liargame.controller;

import com.api.liargame.controller.dto.response.ResponseDto;
import com.api.liargame.controller.dto.response.ResponseDto.ResponseStatus;
import com.api.liargame.exception.DuplicateUserNicknameException;
import com.api.liargame.exception.GameRoomCreateFailException;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.exception.SettingPermissionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

  @ExceptionHandler({
      NotFoundGameRoomException.class,
      DuplicateUserNicknameException.class,
      GameRoomCreateFailException.class,
      SettingPermissionException.class,
      IllegalStateException.class,
      IllegalArgumentException.class
  })
  public ResponseEntity<Object> RuntimeException(final RuntimeException ex) {
    ResponseDto<Object> response = ResponseDto.builder()
        .status(ResponseStatus.FAILURE)
        .message(ex.getMessage())
        .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}
