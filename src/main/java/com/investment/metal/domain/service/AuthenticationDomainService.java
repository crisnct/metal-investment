package com.investment.metal.domain.service;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.valueobject.LoginAttempt;
import com.investment.metal.domain.valueobject.Token;
import com.investment.metal.domain.exception.BusinessException;

import java.util.Optional;

/**
 * Domain service for authentication business logic.
 * Follows Domain-Driven Design principles by encapsulating complex business rules.
 */
public interface AuthenticationDomainService {

    /**
     * Validate user credentials
     */
    boolean validateCredentials(User user, String password);

    /**
     * Generate secure token for user
     */
    Token generateToken(User user);

    /**
     * Validate token and return user
     */
    Optional<User> validateToken(String tokenValue);

    /**
     * Record failed login attempt
     */
    void recordFailedAttempt(Integer userId, String ipAddress);

    /**
     * Check if user is banned
     */
    boolean isUserBanned(Integer userId);

    /**
     * Check if IP is blocked
     */
    boolean isIpBlocked(String ipAddress);

    /**
     * Generate validation code
     */
    int generateValidationCode(boolean strongCode);

    /**
     * Validate user account with code
     */
    void validateAccount(User user, int code) throws BusinessException;

    /**
     * Check if user needs validation
     */
    boolean needsValidation(User user);

    /**
     * Get login attempts for user
     */
    LoginAttempt getLoginAttempts(Integer userId);

    /**
     * Reset failed attempts for user
     */
    void resetFailedAttempts(Integer userId);

    /**
     * Check if token is expired
     */
    boolean isTokenExpired(Token token);

    /**
     * Extend token expiration
     */
    Token extendToken(Token token);
}
