package com.api.liargame.controller.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounterResponseDto {
    private String gameStatus;
    private long count;

    public CounterResponseDto(long count, String gameStatus) {
        this.gameStatus = gameStatus;
        this.count = count;

    }
}
