package com.investment.metal.infrastructure.service;

import jakarta.annotation.PostConstruct;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

/**
 * Infrastructure service for internationalization (i18n) message handling.
 * Provides localized message retrieval based on configured language and country.
 * Follows Clean Architecture principles by keeping message infrastructure concerns separate.
 */
@Service
public class MessageService {

    /**
     * User language configuration from application properties
     */
    @Value("${spring.user.language}")
    private String userLanguage;

    /**
     * User country configuration from application properties
     */
    @Value("${spring.user.country}")
    private String userCountry;

    /**
     * Spring message source for loading localized messages
     */
    private ReloadableResourceBundleMessageSource messageSource;

    /**
     * Current locale based on language and country configuration
     */
    private Locale locale;

    /**
     * Initialize the message service after dependency injection.
     * Sets up the locale and message source for internationalization.
     */
    @PostConstruct
    public void init() {
        locale = new Locale(userLanguage, userCountry);
        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
    }

    /**
     * Get a localized message by key without arguments.
     * 
     * @param key the message key to retrieve
     * @return the localized message string
     */
    public String getMessage(String key) {
        return getMessage(key, null);
    }

    /**
     * Get a localized message by key with arguments for parameter substitution.
     * If the message key is not found, returns a fallback message.
     * 
     * @param key the message key to retrieve
     * @param arguments variable arguments for message parameter substitution
     * @return the localized message string with arguments substituted
     */
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
