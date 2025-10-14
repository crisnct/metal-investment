package com.investment.metal.domain.service.impl;

import com.investment.metal.common.MetalType;
import com.investment.metal.domain.service.MetalInvestmentDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementation of MetalInvestmentDomainService.
 * Handles metal investment business logic.
 */
@Slf4j
@Service
public class MetalInvestmentDomainServiceImpl implements MetalInvestmentDomainService {

    @Override
    public MetalType getMetalType(String symbol) {
        try {
            return MetalType.valueOf(symbol.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid metal symbol: {}", symbol);
            return null;
        }
    }

    @Override
    public boolean isValidMetalSymbol(String symbol) {
        return getMetalType(symbol) != null;
    }

    @Override
    public BigDecimal calculatePriceWithFees(BigDecimal basePrice, MetalType metalType) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Add 2% fee for all metals
        BigDecimal feeMultiplier = BigDecimal.valueOf(1.02);
        return basePrice.multiply(feeMultiplier);
    }

    @Override
    public boolean isWithinPurchaseLimits(BigDecimal amount, MetalType metalType) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Set different limits for different metals
        BigDecimal maxAmount;
        switch (metalType) {
            case GOLD:
                maxAmount = BigDecimal.valueOf(1000); // 1000 ounces max
                break;
            case SILVER:
                maxAmount = BigDecimal.valueOf(10000); // 10000 ounces max
                break;
            case PLATINUM:
                maxAmount = BigDecimal.valueOf(500); // 500 ounces max
                break;
            default:
                maxAmount = BigDecimal.valueOf(200); // 200 ounces max for other metals
                break;
        }
        
        return amount.compareTo(maxAmount) <= 0;
    }

    @Override
    public BigDecimal calculateRecommendedAmount(BigDecimal availableFunds, MetalType metalType) {
        if (availableFunds == null || availableFunds.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Recommend using 80% of available funds
        return availableFunds.multiply(BigDecimal.valueOf(0.8));
    }
}
