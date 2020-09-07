package com.investment.metal.dto;

import lombok.Getter;


public class SimpleMessageDto {

    @Getter
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(String messageFormat, Object... args) {
        this.message = String.format(messageFormat, args);
    }
}
