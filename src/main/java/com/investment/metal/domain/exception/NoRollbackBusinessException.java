package com.investment.metal.domain.exception;

/**
 * Domain exception that should not trigger transaction rollback.
 * This is a domain-level exception for business rules that should not rollback transactions.
 * Follows DDD principles by keeping domain exceptions in the domain layer.
 */
public class NoRollbackBusinessException extends BusinessException {

    public NoRollbackBusinessException(int statusCode, String message) {
        super(statusCode, message);
    }

    public NoRollbackBusinessException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}
