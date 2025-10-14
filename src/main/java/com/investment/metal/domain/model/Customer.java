package com.investment.metal.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Domain model for Customer.
 * Represents a user account in the system.
 */
@Data
@Builder
public class Customer {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private boolean validated;
    private boolean active;
    
    /**
     * Check if customer is valid
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }
    
    /**
     * Check if customer can perform actions
     */
    public boolean canPerformActions() {
        return active && validated;
    }
    
    /**
     * Validate username format
     */
    public boolean isValidUsername() {
        return username != null && 
               username.length() >= 3 && 
               username.length() <= 50 &&
               username.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * Validate email format
     */
    public boolean isValidEmail() {
        return email != null && 
               email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
}
