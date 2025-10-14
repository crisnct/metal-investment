package com.investment.metal.application.repository;

import com.investment.metal.domain.model.MetalPrice;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MetalPrice domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface MetalPriceRepository {
    
    /**
     * Find metal price by ID
     */
    Optional<MetalPrice> findById(Integer id);
    
    /**
     * Find latest metal price by metal type
     */
    Optional<MetalPrice> findLatestByMetalType(String metalType);
    
    /**
     * Find all metal prices
     */
    List<MetalPrice> findAll();
    
    /**
     * Find metal prices by metal type
     */
    List<MetalPrice> findByMetalType(String metalType);
    
    /**
     * Save metal price
     */
    MetalPrice save(MetalPrice metalPrice);
    
    /**
     * Delete metal price by ID
     */
    void deleteById(Integer id);
    
    /**
     * Delete old metal prices (older than specified days)
     */
    void deleteOldPrices(int daysOld);
}
