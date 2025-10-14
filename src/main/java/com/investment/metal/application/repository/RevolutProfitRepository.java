package com.investment.metal.application.repository;

import com.investment.metal.domain.model.RevolutProfit;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RevolutProfit domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface RevolutProfitRepository {
    
    /**
     * Find RevolutProfit by ID
     */
    Optional<RevolutProfit> findById(Integer id);
    
    /**
     * Find all RevolutProfits
     */
    List<RevolutProfit> findAll();
    
    /**
     * Find RevolutProfits by metal type
     */
    List<RevolutProfit> findByMetalType(String metalType);
    
    /**
     * Find latest RevolutProfit by metal type
     */
    Optional<RevolutProfit> findLatestByMetalType(String metalType);
    
    /**
     * Save RevolutProfit
     */
    RevolutProfit save(RevolutProfit revolutProfit);
    
    /**
     * Delete RevolutProfit by ID
     */
    void deleteById(Integer id);
    
    /**
     * Delete old RevolutProfits (older than specified days)
     */
    void deleteOldProfits(int daysOld);
}
