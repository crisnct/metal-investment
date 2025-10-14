package com.investment.metal.infrastructure.exception;

import com.investment.metal.exceptions.BusinessException;

public interface ExceptionSupplier {
    BusinessException create(int code, String message, Throwable cause);
}
