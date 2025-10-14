package com.investment.metal.infrastructure.exception;

/**
 * Infrastructure exception for database-related failures.
 * This exception should be thrown when database operations fail.
 * Follows Clean Architecture principles by keeping infrastructure exceptions in the infrastructure layer.
 */
public class DatabaseException extends TechnicalException {

    public DatabaseException(String message) {
        super(500, message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(500, message, cause);
    }
}
