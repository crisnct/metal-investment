package com.investment.metal.application.repository;

import com.investment.metal.domain.model.Currency;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Currency domain entities.
 * Follows Repository pattern and Clean Architecture principles.
 */
public interface CurrencyRepository {
    
    /**
     * Find currency by ID
     */
    Optional<Currency> findById(Integer id);
    
    /**
     * Find currency by currency code
     */
    Optional<Currency> findByCurrencyCode(String currencyCode);
    
    /**
     * Find all currencies
     */
    List<Currency> findAll();
    
    /**
     * Save currency
     */
    Currency save(Currency currency);
    
    /**
     * Delete currency by ID
     */
    void deleteById(Integer id);
    
    /**
     * Check if currency exists by code
     */
    boolean existsByCurrencyCode(String currencyCode);
}
