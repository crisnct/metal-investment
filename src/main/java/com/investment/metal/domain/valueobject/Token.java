package com.investment.metal.domain.valueobject;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object for authentication tokens.
 * Immutable and encapsulates token-related business logic.
 */
@Data
@Builder
public class Token {
    
    private final String value;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final Integer userId;
    private final TokenType type;

    public enum TokenType {
        LOGIN, RESET_PASSWORD, VALIDATION
    }

    /**
     * Create new login token
     */
    public static Token createLoginToken(Integer userId, int expirationDays) {
        LocalDateTime now = LocalDateTime.now();
        return Token.builder()
            .value(UUID.randomUUID().toString())
            .createdAt(now)
            .expiresAt(now.plusDays(expirationDays))
            .userId(userId)
            .type(TokenType.LOGIN)
            .build();
    }

    /**
     * Create new reset password token
     */
    public static Token createResetPasswordToken(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return Token.builder()
            .value(UUID.randomUUID().toString())
            .createdAt(now)
            .expiresAt(now.plusHours(24)) // Reset tokens expire in 24 hours
            .userId(userId)
            .type(TokenType.RESET_PASSWORD)
            .build();
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token is valid (not expired and has value)
     */
    public boolean isValid() {
        return value != null && !value.isEmpty() && !isExpired();
    }

    /**
     * Get time until expiration in minutes
     */
    public long getMinutesUntilExpiration() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
