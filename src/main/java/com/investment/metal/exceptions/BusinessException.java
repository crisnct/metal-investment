package com.investment.metal.exceptions;

import org.springframework.core.NestedRuntimeException;

public class BusinessException extends NestedRuntimeException {

    private final int statusCode;

    public BusinessException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public BusinessException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}