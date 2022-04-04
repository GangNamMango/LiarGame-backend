package com.api.liargame.controller.dto.request;

import com.api.liargame.domain.Setting;
import com.api.liargame.domain.User;
import com.api.liargame.domain.User.GameRole;
import com.api.liargame.domain.User.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettingRequestDto {
    private Setting setting;
    private String id;
}
