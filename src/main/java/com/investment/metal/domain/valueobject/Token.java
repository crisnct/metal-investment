package com.investment.metal.domain.valueobject;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Value object representing an authentication token.
 * Follows Domain-Driven Design principles for value objects.
 */
@Data
@Builder(toBuilder = true)
public class Token {
    
    private final String value;
    private final LocalDateTime expiresAt;
    private final Integer userId;
    private final TokenType type;
    
    /**
     * Token types for different purposes
     */
    public enum TokenType {
        AUTHENTICATION,
        VALIDATION,
        RESET_PASSWORD
    }
    
    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if token is valid (not expired)
     */
    public boolean isValid() {
        return !isExpired();
    }
}

