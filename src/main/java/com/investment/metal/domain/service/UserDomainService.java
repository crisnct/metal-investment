package com.investment.metal.domain.service;

import com.investment.metal.domain.model.User;

import java.util.Optional;

/**
 * Domain service for User business logic.
 * Encapsulates complex business rules that don't belong to a single entity.
 * Follows Domain-Driven Design principles.
 */
public interface UserDomainService {
    
    /**
     * Validate user credentials
     */
    boolean validateCredentials(User user, String password);
    
    /**
     * Check if user can perform actions (not banned, validated, active)
     */
    boolean canUserPerformActions(User user);
    
    /**
     * Validate user registration data
     */
    boolean isValidRegistrationData(String username, String email, String password);
    
    /**
     * Check if user account is banned
     */
    boolean isUserBanned(User user);
    
    /**
     * Check if IP address is blocked for user
     */
    boolean isIpBlockedForUser(User user, String ipAddress);
    
    /**
     * Record failed login attempt
     */
    void recordFailedAttempt(User user, String ipAddress);
    
    /**
     * Check if user has exceeded maximum failed attempts
     */
    boolean hasExceededMaxFailedAttempts(User user);
    
    /**
     * Generate validation code for user
     */
    int generateValidationCode(User user);
    
    /**
     * Validate user account with code
     */
    boolean validateUserAccount(User user, int code);
    
    /**
     * Generate login token for user
     */
    String generateLoginToken(User user);
    
    /**
     * Generate password reset token for user
     */
    String generatePasswordResetToken(User user);
    
    /**
     * Validate token and return user
     */
    Optional<User> validateToken(String token);
    
    /**
     * Logout user (invalidate token)
     */
    void logoutUser(User user);
}
