package com.investment.metal.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ExceptionDto {

    private int statusCode;

    private String message;

    public ExceptionDto(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
