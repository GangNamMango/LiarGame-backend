package com.api.liargame.service;

import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.exception.NotFoundGameRoomException;
import com.api.liargame.exception.SettingPermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService{

    @Override
    public void checkPermission(GameRoom gameRoom, String userId){
        if (gameRoom == null)
            throw new NotFoundGameRoomException("방이 존재하지 않습니다.");

        if (!gameRoom.getHost().getId().equals(userId))
            throw new SettingPermissionException("방의 호스트가 아닙니다.");
    }

    @Override
    public Setting updateSetting(GameRoom gameRoom, SettingRequestDto settingRequestDto) {
        checkPermission(gameRoom, settingRequestDto.getUserId());

        gameRoom.setSettings(settingRequestDto.getSetting());
        gameRoom.update();

        return gameRoom.getSettings();
    }
}
