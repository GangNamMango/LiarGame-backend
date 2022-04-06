package com.api.liargame.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.api.liargame.controller.dto.request.EnterRequestDto;
import com.api.liargame.controller.dto.request.SettingRequestDto;
import com.api.liargame.controller.dto.request.UserRequestDto;
import com.api.liargame.domain.GameRoom;
import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.Role;
import com.api.liargame.exception.SettingPermissionException;
import com.api.liargame.repository.GameRoomRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SettingServiceTest {

    @Autowired
    SettingService settingService;

    @Autowired
    GameRoomService gameRoomService;

    @Autowired
    GameRoomRepository gameRoomRepository;

    GameRoom room;
    User host;
    User guest;

    @BeforeEach
    void beforeEach() {

        User user = User.builder()
        .nickname("user1")
        .character("ch1")
        .role(Role.HOST)
        .build();
        Setting setting = new Setting();
        GameRoom gameRoom = GameRoom.builder()
            .host(user)
            .setting(setting)
                .build();
        String roomId = gameRoomRepository.save(gameRoom);
        UserRequestDto userRequestDto = new UserRequestDto("user2", "ch2");
        EnterRequestDto enterRequestDto = new EnterRequestDto(roomId, userRequestDto);

        gameRoomService.enter(enterRequestDto);

        room = gameRoom;

        host = gameRoom.getHost();
        guest = gameRoom.getUsers().stream().filter(u-> !u.getId().equals(host.getId())).findFirst().get();
    }


    @Test
    @DisplayName("방장은 설정을 바꿀 수 있다.")
    void updateSetting() {
        Setting setting = new Setting();
        setting.setCapacity(5);
        setting.setTimeLimit(6);
        setting.setTopic("abc");
        SettingRequestDto settingRequestDto = new SettingRequestDto(room.getRoomId(), host.getId(), setting);

        settingService.checkPermission(room, host.getId());
        Setting newSetting = settingService.updateSetting(room, settingRequestDto);
        assertThat(newSetting.getTopic()).isEqualTo(newSetting.getTopic());
        assertThat(newSetting.getTimeLimit()).isEqualTo(newSetting.getTimeLimit());
        assertThat(newSetting.getCapacity()).isEqualTo(newSetting.getCapacity());

    }

    @Test
    @DisplayName("해당 방의 호스트가 아니면 설정을 바꿀 수 없다.")
    void checkPermission() {
        Assertions.assertThrows(SettingPermissionException.class,()->settingService.checkPermission(room, guest.getId()));
    }
}
