package com.investment.metal.infrastructure.exception;

/**
 * Infrastructure exception for technical failures.
 * This exception should be thrown when technical/infrastructure issues occur.
 * Follows Clean Architecture principles by keeping infrastructure exceptions in the infrastructure layer.
 */
public class TechnicalException extends RuntimeException {

    private final int statusCode;

    public TechnicalException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public TechnicalException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
