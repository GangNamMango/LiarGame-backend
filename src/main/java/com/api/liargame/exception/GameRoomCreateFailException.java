package com.api.liargame.exception;

public class GameRoomCreateFailException extends RuntimeException{

  public GameRoomCreateFailException() {
    super("Game Room Create Fail");
  }

  public GameRoomCreateFailException(String message) {
    super(message);
  }

  public GameRoomCreateFailException(String message, Throwable cause) {
    super(message, cause);
  }

  public GameRoomCreateFailException(Throwable cause) {
    super(cause);
  }

  public GameRoomCreateFailException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
