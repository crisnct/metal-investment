package com.investment.metal.application.repository.impl;

import com.investment.metal.application.repository.UserApplicationRepository;
import com.investment.metal.domain.model.User;
import com.investment.metal.infrastructure.persistence.repository.CustomerRepository;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementation of UserApplicationRepository.
 * Bridges between application layer and infrastructure layer.
 * Follows Clean Architecture principles.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserApplicationRepositoryImpl implements UserApplicationRepository {
    
    private final CustomerRepository customerRepository;
    
    @Override
    public Optional<User> findByUsername(String username) {
        try {
            Optional<Customer> customerOpt = customerRepository.findByUsername(username);
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(User.fromEntity(customerOpt.get()));
        } catch (Exception e) {
            log.warn("Failed to find user by username: {}", username, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        try {
            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(User.fromEntity(customerOpt.get()));
        } catch (Exception e) {
            log.warn("Failed to find user by email: {}", email, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> findById(Integer id) {
        try {
            Optional<Customer> customerOpt = customerRepository.findById(id.longValue());
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(User.fromEntity(customerOpt.get()));
        } catch (Exception e) {
            log.warn("Failed to find user by id: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public User save(User user) {
        try {
            Customer customer = user.toEntity();
            Customer savedCustomer = customerRepository.save(customer);
            return User.fromEntity(savedCustomer);
        } catch (Exception e) {
            log.error("Failed to save user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }
    
    @Override
    public void deleteById(Integer id) {
        try {
            customerRepository.deleteById(id.longValue());
        } catch (Exception e) {
            log.error("Failed to delete user by id: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    @Override
    public boolean existsByUsername(String username) {
        try {
            return customerRepository.findByUsername(username).isPresent();
        } catch (Exception e) {
            log.warn("Failed to check if username exists: {}", username, e);
            return false;
        }
    }
    
    @Override
    public boolean existsByEmail(String email) {
        try {
            return customerRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            log.warn("Failed to check if email exists: {}", email, e);
            return false;
        }
    }
    
    @Override
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        try {
            Optional<Customer> customerOpt = customerRepository.findByUsernameAndPassword(username, password);
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(User.fromEntity(customerOpt.get()));
        } catch (Exception e) {
            log.warn("Failed to find user by username and password: {}", username, e);
            return Optional.empty();
        }
    }
}
