package com.investment.metal.infrastructure.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Infrastructure service for password operations.
 * Handles password hashing and validation using Spring Security.
 * Follows Clean Architecture principles by keeping infrastructure concerns separate.
 */
@Service
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Hash password using Spring Security encoder
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    /**
     * Validate password against hash
     */
    public boolean validatePassword(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }
}
