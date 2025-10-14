package com.investment.metal.application.repository;

import com.investment.metal.domain.model.Customer;

import java.util.Optional;

/**
 * Repository interface for Customer domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface CustomerRepository {
    
    /**
     * Find customer by ID
     */
    Optional<Customer> findById(Long id);
    
    /**
     * Find customer by username
     */
    Optional<Customer> findByUsername(String username);
    
    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find customer by username and password
     */
    Optional<Customer> findByUsernameAndPassword(String username, String password);
    
    /**
     * Save customer
     */
    Customer save(Customer customer);
    
    /**
     * Delete customer by ID
     */
    void deleteById(Long id);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
