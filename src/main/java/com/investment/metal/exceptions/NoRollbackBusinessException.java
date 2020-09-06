package com.investment.metal.exceptions;

public class NoRollbackBusinessException extends BusinessException{
    public NoRollbackBusinessException(int statusCode, String message) {
        super(statusCode, message);
    }

    public NoRollbackBusinessException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}
