package com.api.liargame.exception;

public class DuplicateUserNicknameException extends RuntimeException{

  public DuplicateUserNicknameException() {
    super("Duplicate User Nickname");
  }

  public DuplicateUserNicknameException(String message) {
    super(message);
  }

  public DuplicateUserNicknameException(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicateUserNicknameException(Throwable cause) {
    super(cause);
  }

  public DuplicateUserNicknameException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
