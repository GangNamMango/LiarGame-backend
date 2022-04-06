package com.api.liargame.service;

import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;

public interface SettingService {
    Setting updateSetting(GameRoom gameRoom, SettingRequestDto settingRequestDto);
    void checkPermission(GameRoom gameRoom, String userId);
}
