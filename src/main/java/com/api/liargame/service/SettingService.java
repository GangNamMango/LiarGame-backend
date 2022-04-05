package com.api.liargame.service;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;

public interface SettingService {
    Setting updateSetting(GameRoom gameRoom, Setting setting); 
    void checkPermission(GameRoom gameRoom, String userId);
}
