package com.investment.metal.exceptions;

import lombok.Data;

@Data
public class SecurityResponse {

    private int statusCode;

    private String message;

    public SecurityResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}