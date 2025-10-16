package com.investment.metal.infrastructure.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Input validation utility to prevent SQL injection and other security vulnerabilities.
 * Provides comprehensive validation for all request header parameters.
 * 
 * @author cristian.tone
 */
@Component
public class InputValidator {

    // Regex patterns for validation
    private static final Pattern IP_PATTERN = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");;
    private static final Pattern METAL_SYMBOL_PATTERN = Pattern.compile("^[A-Z]{2,20}$");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^[a-zA-Z0-9+\\-*/().\\s]+$");
    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("^(DAILY|WEEKLY|MONTHLY)$");
    private static final Pattern REASON_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,!?-]{1,200}$");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(?:select|update|delete|insert|drop|union|alter|truncate|exec|execute)\\b|--|;|/\\*|\\*/|(?:'|\")\\s*(?:or|and)\\b|\\b(?:or|and)\\b\\s*\\d+\\s*=\\s*\\d+|\\b(?:or|and)\\b\\s*'[^']*'\\s*=\\s*'[^']*')",
        Pattern.CASE_INSENSITIVE
    );

  /**
     * Validates IP address format and prevents SQL injection.
     * 
     * @param ip the IP address to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(ip)) {
            return false;
        }
        
        // Validate IP format
        return IP_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * Validates username format and prevents SQL injection.
     * 
     * @param username the username to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(username)) {
            return false;
        }
        
        // Validate username format
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validates email format and prevents SQL injection.
     * 
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = email.trim();
        // Validate email format
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        
        // Additional protection: reject obvious SQL keywords
        return !containsSqlInjection(trimmed);
    }

    /**
     * Validates metal symbol format and prevents SQL injection.
     * 
     * @param metalSymbol the metal symbol to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMetalSymbol(String metalSymbol) {
        if (metalSymbol == null || metalSymbol.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(metalSymbol)) {
            return false;
        }
        
        // Validate metal symbol format
        return METAL_SYMBOL_PATTERN.matcher(metalSymbol.trim()).matches();
    }

    /**
     * Validates mathematical expression format and prevents SQL injection.
     * 
     * @param expression the expression to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(expression)) {
            return false;
        }
        
        // Validate expression format
        return EXPRESSION_PATTERN.matcher(expression.trim()).matches();
    }

    /**
     * Validates frequency format and prevents SQL injection.
     * 
     * @param frequency the frequency to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(frequency)) {
            return false;
        }
        
        // Validate frequency format
        return FREQUENCY_PATTERN.matcher(frequency.trim()).matches();
    }

    /**
     * Validates reason format and prevents SQL injection.
     * 
     * @param reason the reason to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(reason)) {
            return false;
        }
        
        // Validate reason format
        return REASON_PATTERN.matcher(reason.trim()).matches();
    }

    /**
     * Validates numeric values to prevent injection.
     * 
     * @param value the numeric value to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(value)) {
            return false;
        }
        
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates integer values to prevent injection.
     * 
     * @param value the integer value to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidIntegerValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(value)) {
            return false;
        }
        
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates password format and prevents SQL injection.
     * 
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(password)) {
            return false;
        }
        
        // Basic password validation (length and character restrictions)
        String trimmed = password.trim();
        return trimmed.length() >= 8 && trimmed.length() <= 128;
    }

    /**
     * Validates token format and prevents SQL injection.
     * 
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Check for SQL injection patterns
        if (containsSqlInjection(token)) {
            return false;
        }
        
        // Basic token validation (alphanumeric and common token characters)
        String trimmed = token.trim();
        return trimmed.length() >= 10 && trimmed.length() <= 500 && 
               trimmed.matches("^[a-zA-Z0-9._-]+$");
    }

    /**
     * Checks if the input contains SQL injection patterns.
     * 
     * @param input the input to check
     * @return true if SQL injection patterns are detected, false otherwise
     */
    private boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        
        Matcher matcher = SQL_INJECTION_PATTERN.matcher(input);
        return matcher.find();
    }

    /**
     * Sanitizes input by removing potentially dangerous characters.
     * 
     * @param input the input to sanitize
     * @return sanitized input
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove SQL injection patterns
        String sanitized = SQL_INJECTION_PATTERN.matcher(input).replaceAll("");
        
        // Trim whitespace
        return sanitized.trim();
    }

    /**
     * Validates that a string is not null, not empty, and within length limits.
     * 
     * @param input the input to validate
     * @param maxLength maximum allowed length
     * @return true if valid, false otherwise
     */
    public boolean isValidString(String input, int maxLength) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        if (input.length() > maxLength) {
            return false;
        }
        
        // Check for SQL injection patterns
        return !containsSqlInjection(input);
    }
}
