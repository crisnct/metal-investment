package com.investment.metal.service;

import com.investment.metal.service.impl.ExceptionService;
import com.investment.metal.service.impl.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractService {

    @Autowired
    protected ExceptionService exceptionService;

    @Autowired
    protected MessageService messageService;

}
