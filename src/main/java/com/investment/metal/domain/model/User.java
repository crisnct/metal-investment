package com.investment.metal.domain.model;

import com.investment.metal.database.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

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
     * Business rule: Username must be between 3-50 characters
     */
    public boolean isValidUsername() {
        return username != null && username.length() >= 3 && username.length() <= 50;
    }

    /**
     * Business rule: Email must be valid format
     */
    public boolean isValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Business rule: User can perform actions only if validated and active
     */
    public boolean canPerformActions() {
        return validated && active;
    }

    /**
     * Factory method to create User from Customer entity
     */
    public static User fromEntity(Customer customer) {
        return User.builder()
            .id(customer.getId())
            .username(customer.getUsername())
            .email(customer.getEmail())
            .createdAt(LocalDateTime.now()) // TODO: Add createdAt to Customer entity
            .validated(true) // TODO: Add validation status to Customer entity
            .active(true) // TODO: Add active status to Customer entity
            .build();
    }

    /**
     * Convert to Customer entity for persistence
     */
    public Customer toEntity() {
        Customer customer = new Customer();
        customer.setId(this.id);
        customer.setUsername(this.username);
        customer.setEmail(this.email);
        return customer;
    }

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
