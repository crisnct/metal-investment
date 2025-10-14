package com.investment.metal.application.repository;

import com.investment.metal.domain.model.MetalPurchase;

import java.util.List;

/**
 * Repository interface for MetalPurchase domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface MetalPurchaseRepository {
    
    /**
     * Find all purchases by user ID
     */
    List<MetalPurchase> findByUserId(Integer userId);
    
    /**
     * Find purchase by ID
     */
    java.util.Optional<MetalPurchase> findById(Integer id);
    
    /**
     * Save purchase
     */
    MetalPurchase save(MetalPurchase purchase);
    
    /**
     * Delete purchase by ID
     */
    void deleteById(Integer id);
    
    /**
     * Find purchases by user ID and metal type
     */
    List<MetalPurchase> findByUserIdAndMetalType(Integer userId, String metalType);
    
    /**
     * Count purchases by user ID
     */
    long countByUserId(Integer userId);
    
    /**
     * Delete purchases by user ID and metal type
     */
    void deleteByUserIdAndMetalType(Integer userId, String metalType);
}
