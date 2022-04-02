package com.api.liargame.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.api.liargame.constants.SettingConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettingTest {

  @Test
  @DisplayName("방 설정을 생성하면 기본값이 지정되어야 한다.")
  public void create() {
    Setting setting = new Setting();

    assertThat(setting.getTimeLimit()).isEqualTo(SettingConstant.DEFAULT_TIME_LIMIT);
    assertThat(setting.getCapacity()).isEqualTo(SettingConstant.DEFAULT_CAPACITY);
    assertThat(setting.getTopic()).isEqualTo(SettingConstant.DEFAULT_TOPIC);
  }

}
