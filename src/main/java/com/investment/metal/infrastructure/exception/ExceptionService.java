package com.investment.metal.infrastructure.exception;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ExceptionService {

    private final MessageService messageService;
    private ExceptionBuilder exceptionBuilder;

    @Autowired
    public ExceptionService(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        this.exceptionBuilder = new ExceptionBuilder();
    }

    public ExceptionBuilder createBuilder(MessageKey key) {
        return this.exceptionBuilder.create(key, this.messageService);
    }

    public BusinessException createException(MessageKey key) {
        return this.exceptionBuilder.create(key, this.messageService).build();
    }

    public void check(boolean condition, MessageKey key) throws BusinessException {
        if (condition) {
            throw createException(key);
        }
    }

    public void check(boolean condition, MessageKey key, Object... args) throws BusinessException {
        if (condition) {
            throw createBuilder(key).setArguments(args).build();
        }
    }

}
