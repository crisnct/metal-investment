package com.investment.metal.domain.exception;

/**
 * Domain exception for validation rule violations.
 * This exception should be thrown when domain validation rules are violated.
 * Follows DDD principles by keeping domain exceptions in the domain layer.
 */
public class DomainValidationException extends BusinessException {

    public DomainValidationException(String message) {
        super(400, message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
