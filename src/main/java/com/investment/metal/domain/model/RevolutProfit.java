package com.investment.metal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model for RevolutProfit.
 * Represents profit calculation data for Revolut metal investments.
 */
@Data
@Builder
public class RevolutProfit {
    
    private Integer id;
    private MetalType metalType;
    private BigDecimal profit;
    private LocalDateTime timestamp;
    private BigDecimal revolutPriceOz;
    private BigDecimal metalPriceOz;
    private BigDecimal currencyToRonRate;
    
    /**
     * Check if RevolutProfit is valid
     */
    public boolean isValid() {
        return metalType != null && 
               profit != null && 
               timestamp != null &&
               revolutPriceOz != null && 
               metalPriceOz != null && 
               currencyToRonRate != null;
    }
    
    /**
     * Check if profit is positive
     */
    public boolean isProfitable() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Calculate profit percentage
     */
    public BigDecimal getProfitPercentage() {
        if (revolutPriceOz == null || metalPriceOz == null || 
            revolutPriceOz.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return profit.divide(revolutPriceOz, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
    }
}
