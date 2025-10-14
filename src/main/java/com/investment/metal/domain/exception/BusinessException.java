package com.investment.metal.domain.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * Domain exception representing business rule violations.
 * This is a domain-level exception that should be thrown when business rules are violated.
 * Follows DDD principles by keeping domain exceptions in the domain layer.
 */
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
