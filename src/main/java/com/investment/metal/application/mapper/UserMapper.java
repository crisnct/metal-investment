package com.investment.metal.application.mapper;

import com.investment.metal.domain.model.User;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Application layer mapper for User domain model and Customer infrastructure entity.
 * Follows Clean Architecture principles by handling mapping between layers.
 */
@Component
public class UserMapper {

    /**
     * Convert Customer entity to User domain model
     */
    public User toDomainModel(Customer customer) {
        if (customer == null) {
            return null;
        }
        
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
     * Convert User domain model to Customer entity
     */
    public Customer toEntity(User user) {
        if (user == null) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setId(user.getId());
        customer.setUsername(user.getUsername());
        customer.setEmail(user.getEmail());
        return customer;
    }
}
