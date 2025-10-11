package com.investment.metal.service.exception;

import com.investment.metal.MessageKey;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.MessageService;

import jakarta.annotation.Nullable;
import java.util.Objects;

public class ExceptionBuilder {

    private MessageKey key;

    private Throwable cause;

    private Object[] arguments;

    private ExceptionSupplier exceptionSupplier;

    private MessageService messageService;

    public ExceptionBuilder create(MessageKey key, MessageService service) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(service);
        ExceptionBuilder builder = new ExceptionBuilder();
        builder.key = key;
        builder.messageService = service;
        return builder;
    }

    public ExceptionBuilder setArguments(Object... args) {
        this.arguments = args;
        return this;
    }

    public ExceptionBuilder setException(ExceptionSupplier exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
        return this;
    }

    public ExceptionBuilder setExceptionCause(@Nullable Throwable cause) {
        this.cause = cause;
        return this;
    }

    public BusinessException build() {
        String message = this.messageService.getMessage(this.key.name(), this.arguments);
        if (this.exceptionSupplier == null) {
            return new BusinessException(this.key.getCode(), message, this.cause);
        } else {
            return this.exceptionSupplier.create(this.key.getCode(), message, this.cause);
        }
    }

}
