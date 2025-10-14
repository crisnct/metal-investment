package com.investment.metal.application.exception;

/**
 * Application layer exception for use case failures.
 * This exception should be thrown when use cases encounter issues.
 * Follows Clean Architecture principles by keeping application exceptions in the application layer.
 */
public class UseCaseException extends ApplicationServiceException {

    public UseCaseException(String message) {
        super(400, message);
    }

    public UseCaseException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
