package com.api.liargame.exception;

public class NotFoundGameRoomException extends RuntimeException{

  public NotFoundGameRoomException() {
    super("Not Found Game Room");
  }

  public NotFoundGameRoomException(String message) {
    super(message);
  }

  public NotFoundGameRoomException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotFoundGameRoomException(Throwable cause) {
    super(cause);
  }

  public NotFoundGameRoomException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
