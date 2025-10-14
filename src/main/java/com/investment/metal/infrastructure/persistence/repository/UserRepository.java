package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.domain.model.User;

import java.util.Optional;

/**
 * Repository interface for User domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface UserRepository {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by ID
     */
    Optional<User> findById(Integer id);
    
    /**
     * Save user
     */
    User save(User user);
    
    /**
     * Delete user by ID
     */
    void deleteById(Integer id);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
