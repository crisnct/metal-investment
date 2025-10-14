package com.investment.metal.domain.exception;

/**
 * Domain exception for insufficient funds scenarios.
 * This exception should be thrown when a user tries to make a purchase
 * but doesn't have sufficient funds.
 * Follows DDD principles by keeping domain exceptions in the domain layer.
 */
public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(String message) {
        super(400, message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
