package com.investment.metal.exceptions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SecurityResponse {

    private int statusCode;

    private String message;

    public SecurityResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}