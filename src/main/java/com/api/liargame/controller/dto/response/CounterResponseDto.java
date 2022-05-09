package com.api.liargame.controller.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounterResponseDto {
    long count;

    public CounterResponseDto(long count) {
        this.count = count;
    }
}
