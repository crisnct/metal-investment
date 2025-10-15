package com.investment.metal.infrastructure.validation;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.exception.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for validating request parameters to prevent SQL injection and other security vulnerabilities.
 * Provides centralized validation logic for all controllers.
 * 
 * @author cristian.tone
 */
@Service
public class ValidationService {

    @Autowired
    private InputValidator inputValidator;

    @Autowired
    private ExceptionService exceptionService;

    /**
     * Validates IP address parameter.
     * 
     * @param ip the IP address to validate
     * @throws BusinessException if validation fails
     */
    public void validateIp(String ip) throws BusinessException {
        if (!inputValidator.isValidIp(ip)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid IP address format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates username parameter.
     * 
     * @param username the username to validate
     * @throws BusinessException if validation fails
     */
    public void validateUsername(String username) throws BusinessException {
        if (!inputValidator.isValidUsername(username)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid username format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates email parameter.
     * 
     * @param email the email to validate
     * @throws BusinessException if validation fails
     */
    public void validateEmail(String email) throws BusinessException {
        if (!inputValidator.isValidEmail(email)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid email format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates metal symbol parameter.
     * 
     * @param metalSymbol the metal symbol to validate
     * @throws BusinessException if validation fails
     */
    public void validateMetalSymbol(String metalSymbol) throws BusinessException {
        if (!inputValidator.isValidMetalSymbol(metalSymbol)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid metal symbol format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates mathematical expression parameter.
     * 
     * @param expression the expression to validate
     * @throws BusinessException if validation fails
     */
    public void validateExpression(String expression) throws BusinessException {
        if (!inputValidator.isValidExpression(expression)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid expression format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates frequency parameter.
     * 
     * @param frequency the frequency to validate
     * @throws BusinessException if validation fails
     */
    public void validateFrequency(String frequency) throws BusinessException {
        if (!inputValidator.isValidFrequency(frequency)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid frequency format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates reason parameter.
     * 
     * @param reason the reason to validate
     * @throws BusinessException if validation fails
     */
    public void validateReason(String reason) throws BusinessException {
        if (!inputValidator.isValidReason(reason)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid reason format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates numeric parameter.
     * 
     * @param value the numeric value to validate
     * @param parameterName the name of the parameter for error messages
     * @throws BusinessException if validation fails
     */
    public void validateNumericValue(String value, String parameterName) throws BusinessException {
        if (!inputValidator.isValidNumericValue(value)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid " + parameterName + " format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates integer parameter.
     * 
     * @param value the integer value to validate
     * @param parameterName the name of the parameter for error messages
     * @throws BusinessException if validation fails
     */
    public void validateIntegerValue(String value, String parameterName) throws BusinessException {
        if (!inputValidator.isValidIntegerValue(value)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid " + parameterName + " format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates password parameter.
     * 
     * @param password the password to validate
     * @throws BusinessException if validation fails
     */
    public void validatePassword(String password) throws BusinessException {
        if (!inputValidator.isValidPassword(password)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid password format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates token parameter.
     * 
     * @param token the token to validate
     * @throws BusinessException if validation fails
     */
    public void validateToken(String token) throws BusinessException {
        if (!inputValidator.isValidToken(token)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid token format or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Validates string parameter with length limit.
     * 
     * @param input the string to validate
     * @param maxLength maximum allowed length
     * @param parameterName the name of the parameter for error messages
     * @throws BusinessException if validation fails
     */
    public void validateString(String input, int maxLength, String parameterName) throws BusinessException {
        if (!inputValidator.isValidString(input, maxLength)) {
            throw exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid " + parameterName + " format, length, or contains potentially dangerous content")
                    .build();
        }
    }

    /**
     * Sanitizes input by removing potentially dangerous characters.
     * 
     * @param input the input to sanitize
     * @return sanitized input
     */
    public String sanitizeInput(String input) {
        return inputValidator.sanitizeInput(input);
    }
}
