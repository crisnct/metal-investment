package com.investment.metal.application.exception;

/**
 * Application layer exception for service-related failures.
 * This exception should be thrown when application services encounter issues.
 * Follows Clean Architecture principles by keeping application exceptions in the application layer.
 */
public class ApplicationServiceException extends RuntimeException {

    private final int statusCode;

    public ApplicationServiceException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public ApplicationServiceException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
