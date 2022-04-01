package com.api.liargame.service;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.NotFoundGameRoomException;
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
    public GameRoom enter(EnterRequestDto enterRequestDto) {
        String roomId = enterRequestDto.getRoomId();
        GameRoom foundGameRoom = gameRoomRepository.findById(roomId);
        if (foundGameRoom == null) {
            throw new NotFoundGameRoomException();
        }

        User user = enterRequestDto.getUser().toEntity();

        foundGameRoom.addUser(user);
        foundGameRoom.update();

        return foundGameRoom;
    }

    @Override
    public GameRoom createdRoom(UserRequestDto userRequestDto){
        String roomId = createGameRoomId();
        String nickname = userRequestDto.getNickname();
        String character = userRequestDto.getNickname();
        User user = User.builder()
            .nickname(nickname)
            .character(character)
            .role(Role.HOST)
            .build();
        Setting defaultSetting = new Setting();
        GameRoom gameRoom = new GameRoom(roomId, user, defaultSetting);
        gameRoomRepository.save(gameRoom);

        return gameRoom;
    }


}
