package com.investment.metal.domain.model;

// Domain model should not depend on infrastructure entities

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

/**
 * Rich domain model for User following Domain-Driven Design principles.
 * Encapsulates business logic and validation rules.
 */
@Data
@Builder(toBuilder = true)
public class User {
    
    private final Integer id;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;
    private final boolean validated;
    private final boolean active;

    /**
     * Business rule: Username must be between 3-50 characters and alphanumeric
     */
    public boolean isValidUsername() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.length() >= 3 && 
               username.length() <= 50 && 
               username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Business rule: Email must be valid format with proper structure
     */
    public boolean isValidEmail() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.contains("@") && 
               email.contains(".") && 
               email.length() >= 5 && 
               email.length() <= 255 &&
               email.matches("^[^@]+@[^@]+\\.[^@]+$");
    }

    /**
     * Business rule: User can perform actions only if validated and active
     */
    public boolean canPerformActions() {
        return validated && active;
    }
    
    /**
     * Business rule: Check if user account is in valid state
     */
    public boolean isAccountValid() {
        return isValidUsername() && isValidEmail() && canPerformActions();
    }
    
    /**
     * Business rule: Check if user can be authenticated
     * A user can be authenticated if they are active and validated
     * 
     * @return true if user can be authenticated, false otherwise
     */
    public boolean canBeAuthenticated() {
        return active && validated;
    }
    
    /**
     * Business rule: Check if account is expired (older than 1 year)
     */
    public boolean isExpired() {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isBefore(java.time.LocalDateTime.now().minusYears(1));
    }

    // Domain models should not contain infrastructure mapping methods
    // Mapping should be handled by application layer mappers

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
