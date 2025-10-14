package com.investment.metal.infrastructure.exception;

import com.investment.metal.domain.exception.BusinessException;

public interface ExceptionSupplier {
    BusinessException create(int code, String message, Throwable cause);
}
