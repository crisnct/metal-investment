package com.investment.metal.domain.exception;

/**
 * Domain exception for invalid metal type scenarios.
 * This exception should be thrown when an invalid metal type is provided.
 * Follows DDD principles by keeping domain exceptions in the domain layer.
 */
public class InvalidMetalTypeException extends BusinessException {

    public InvalidMetalTypeException(String message) {
        super(400, message);
    }

    public InvalidMetalTypeException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
