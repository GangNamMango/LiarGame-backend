package com.api.liargame.service;

import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService{
    private final GameRoomRepository gameRoomRepository;

    @Override
    public String createGameRoomId() {
        while(true){
            String roomId = gameRoomRepository.createRoomId();
            GameRoom existRoomId = gameRoomRepository.findById(roomId);
            if(existRoomId == null) return roomId;
        }
    }

    @Override
    public GameRoom createdRoom(UserRequestDto userRequestDto){
        String roomId = createGameRoomId();
        String nickname = userRequestDto.getNickname();
        String character = userRequestDto.getNickname();
        User user = new User(nickname, User.Role.HOST, null, character); // 유저를 들어올 때 생성할 경우 ..
        Setting defaultSetting = new Setting(); // 세팅은 디폴트나 받은걸로 할 수 있게..
        defaultSetting.setMaxUser(5);
        GameRoom gameRoom = new GameRoom(roomId, user, defaultSetting);
        gameRoomRepository.save(gameRoom);
        return gameRoom;

        //        gameRoomRepository.save(gameRoom);
//        return gameRoom;
    }


}
