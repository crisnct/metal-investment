package com.investment.metal.application.repository.impl;

import com.investment.metal.application.repository.UserRepository;
import com.investment.metal.database.Customer;
import com.investment.metal.database.CustomerRepository;
import com.investment.metal.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementation of UserRepository.
 * Bridges between domain layer and infrastructure layer.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

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
            Customer customer = customerRepository.findById(id.longValue()).orElse(null);
            if (customer == null) {
                return Optional.empty();
            }
            return Optional.of(User.fromEntity(customer));
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
}
