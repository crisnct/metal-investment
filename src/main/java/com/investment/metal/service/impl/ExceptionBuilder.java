package com.investment.metal.service.impl;

import com.investment.metal.MessageKey;
import com.investment.metal.exceptions.BusinessException;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

class ExceptionBuilder {

    private MessageKey key;

    private Throwable cause;

    private Object[] arguments;

    private ExceptionSupplier exceptionSupplier;

    private MessageService messageService;

    public ExceptionBuilder create(MessageKey key, MessageService service) {
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


    public ExceptionBuilder setExceptionCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public BusinessException build() {
        String message = messageService.getMessage(key.name(), this.arguments);
        if (this.exceptionSupplier == null){
            return new BusinessException(key.getCode(), message, cause);
        }else{
            return this.exceptionSupplier.create(key.getCode(), message, cause);
        }
    }

}