package com.investment.metal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Service
public class MessageService {

    @Value("${spring.user.language}")
    private String userLanguage;

    @Value("${spring.user.country}")
    private String userCountry;

    @Autowired
    private MessageSource bundle;

    private Locale locale;

    @PostConstruct
    public void init() {
        locale = new Locale(userLanguage, userCountry);
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, Object... arguments) {
        String message;
        try {
            message = this.bundle.getMessage(key, arguments, locale);
        } catch (NoSuchMessageException e) {
            message = "Undefined message for key " + key;
        }
        return message;
    }

}
