package com.api.liargame.exception;

public class SettingPermissionException extends RuntimeException {
    
    public SettingPermissionException() {
      super("Setting Access Denied");
    }
  
    public SettingPermissionException(String message) {
      super(message);
    }
  
    public SettingPermissionException(String message, Throwable cause) {
      super(message, cause);
    }
  
    public SettingPermissionException(Throwable cause) {
      super(cause);
    }
  
    public SettingPermissionException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }
}
