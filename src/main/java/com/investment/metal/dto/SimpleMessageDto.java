package com.investment.metal.dto;

import lombok.Getter;


public class SimpleMessageDto {

    @Getter
    private final String message;

    public SimpleMessageDto(String message) {
        this.message = message;
    }

    public SimpleMessageDto(String messageFormat, Object... args) {
        this.message = String.format(messageFormat, args);
    }

}
