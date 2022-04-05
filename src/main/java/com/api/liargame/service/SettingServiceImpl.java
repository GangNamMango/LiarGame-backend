package com.api.liargame.service;

import java.util.Optional;

import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.exception.SettingPermissionException;
import com.api.liargame.repository.GameRoomRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService{
    // private final GameRoomService gameRoomService;
    // private final GameRoomRepository gameRoomRepository;

    @Override
    public void checkPermission(GameRoom gameRoom, String userId){
        if(gameRoom.getHost().getId().equals(userId)){
            return ;
        }
        throw new SettingPermissionException("방의 호스트가 아닙니다.");
    }
    
    
    @Override
    public Setting updateSetting(GameRoom gameRoom, Setting setting) {
        gameRoom.setSettings(setting);
        gameRoom.update();
        return gameRoom.getSettings();
    }
}
