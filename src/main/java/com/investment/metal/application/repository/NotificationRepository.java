package com.investment.metal.application.repository;

import com.investment.metal.domain.model.Notification;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface NotificationRepository {
    
    /**
     * Find notification by ID
     */
    Optional<Notification> findById(Integer id);
    
    /**
     * Find notification by user ID
     */
    Optional<Notification> findByUserId(Integer userId);
    
    /**
     * Find all notifications
     */
    List<Notification> findAll();
    
    /**
     * Save notification
     */
    Notification save(Notification notification);
    
    /**
     * Delete notification by ID
     */
    void deleteById(Integer id);
    
    /**
     * Delete notification by user ID
     */
    void deleteByUserId(Integer userId);
    
    /**
     * Check if notification exists for user
     */
    boolean existsByUserId(Integer userId);
}
