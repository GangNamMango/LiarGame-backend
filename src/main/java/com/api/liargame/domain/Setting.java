package com.api.liargame.domain;

import com.api.liargame.constants.SettingConstant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Setting {

  private Integer timeLimit;
  private Integer capacity;
  private String topic;


  public Setting() {
    this.timeLimit = SettingConstant.DEFAULT_TIME_LIMIT;
    this.capacity = SettingConstant.DEFAULT_CAPACITY;
    this.topic = SettingConstant.DEFAULT_TOPIC;
  }
}
