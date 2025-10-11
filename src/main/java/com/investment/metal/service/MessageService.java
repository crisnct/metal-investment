package com.investment.metal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Locale;

@Service
public class MessageService {

    @Value("${spring.user.language}")
    private String userLanguage;

    @Value("${spring.user.country}")
    private String userCountry;

    private ReloadableResourceBundleMessageSource messageSource;

    private Locale locale;

    @PostConstruct
    public void init() {
        locale = new Locale(userLanguage, userCountry);
        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, Object... arguments) {
        String message;
        try {
            message = this.messageSource.getMessage(key, arguments, locale);
        } catch (NoSuchMessageException e) {
            message = "Undefined message for key " + key;
        }
        return message;
    }

}
