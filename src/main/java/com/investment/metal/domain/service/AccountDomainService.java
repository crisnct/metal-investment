package com.investment.metal.domain.service;

import com.investment.metal.domain.model.User;

import java.util.Optional;

/**
 * Domain service for account-related business logic.
 * Encapsulates complex business rules for user account management.
 * Follows Domain-Driven Design principles.
 */
public interface AccountDomainService {
    
    /**
     * Validate user registration data
     */
    boolean isValidRegistrationData(String username, String email, String password);
    
    /**
     * Check if username is available
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Check if email is available
     */
    boolean isEmailAvailable(String email);
    
    /**
     * Validate password strength
     */
    boolean isValidPassword(String password);
    
    /**
     * Check if user account is active
     */
    boolean isAccountActive(User user);
    
    /**
     * Check if user account is validated
     */
    boolean isAccountValidated(User user);
    
    /**
     * Check if user can register
     */
    boolean canUserRegister(String username, String email);
    
    /**
     * Generate secure password hash
     */
    String hashPassword(String password);
    
    /**
     * Validate password against hash
     */
    boolean validatePassword(String password, String hash);
}
