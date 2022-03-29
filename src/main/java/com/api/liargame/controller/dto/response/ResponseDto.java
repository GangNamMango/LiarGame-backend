package com.api.liargame.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResponseDto<T> {
  public interface ResponseStatus {
    String SUCCESS = "success";
    String FAILURE = "failure";
  }

  private String status;
  private String message;
  private T data;
}
