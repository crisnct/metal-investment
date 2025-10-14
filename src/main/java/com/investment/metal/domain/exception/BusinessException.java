package com.investment.metal.domain.exception;

import lombok.Getter;

/**
 * Domain exception representing business rule violations.
 * This is a domain-level exception that should be thrown when business rules are violated.
 * Follows DDD principles by keeping domain exceptions in the domain layer.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int statusCode;

    public BusinessException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public BusinessException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}
