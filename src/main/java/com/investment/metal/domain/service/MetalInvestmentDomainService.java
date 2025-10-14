package com.investment.metal.domain.service;

import com.investment.metal.common.MetalType;

/**
 * Domain service for metal investment business logic.
 * Encapsulates complex business rules that don't belong to a single entity.
 */
public interface MetalInvestmentDomainService {
    
    /**
     * Get metal type from symbol
     */
    MetalType getMetalType(String symbol);
    
    /**
     * Validate metal symbol
     */
    boolean isValidMetalSymbol(String symbol);
    
    /**
     * Calculate metal price with fees
     */
    java.math.BigDecimal calculatePriceWithFees(java.math.BigDecimal basePrice, MetalType metalType);
    
    /**
     * Check if purchase is within limits
     */
    boolean isWithinPurchaseLimits(java.math.BigDecimal amount, MetalType metalType);
    
    /**
     * Calculate recommended purchase amount
     */
    java.math.BigDecimal calculateRecommendedAmount(java.math.BigDecimal availableFunds, MetalType metalType);
}
