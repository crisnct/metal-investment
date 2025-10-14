package com.investment.metal.infrastructure.mapper;

import com.investment.metal.domain.model.User;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Infrastructure mapper for converting between User domain model and Customer entity.
 * Follows Clean Architecture principles by keeping mapping logic in infrastructure layer.
 */
@Component
public class UserMapper {

    /**
     * Convert Customer entity to User domain model.
     * 
     * @param customer the JPA entity
     * @return the domain model
     */
    public User toDomainModel(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        return User.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .createdAt(LocalDateTime.now()) // Default value since Customer doesn't have createdAt
                .validated(true) // Default value since Customer doesn't have validation status
                .active(true) // Default value since Customer doesn't have active status
                .build();
    }

    /**
     * Convert User domain model to Customer entity.
     * 
     * @param user the domain model
     * @return the JPA entity
     */
    public Customer toEntity(User user) {
        if (user == null) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setId(user.getId());
        customer.setUsername(user.getUsername());
        customer.setEmail(user.getEmail());
        // Note: password is handled separately for security
        return customer;
    }

    /**
     * Update Customer entity with User domain model data.
     * 
     * @param customer the existing entity to update
     * @param user the domain model with new data
     */
    public void updateEntity(Customer customer, User user) {
        if (customer == null || user == null) {
            return;
        }
        
        customer.setUsername(user.getUsername());
        customer.setEmail(user.getEmail());
    }
}
