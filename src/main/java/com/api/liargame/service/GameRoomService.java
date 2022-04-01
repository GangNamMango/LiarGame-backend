package com.api.liargame.service;

import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.repository.GameRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface GameRoomService {

    GameRoom createdRoom(UserRequestDto userRequestDto);
    
    String createGameRoomId();
}
