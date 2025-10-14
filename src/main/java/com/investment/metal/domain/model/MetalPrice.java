package com.investment.metal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model for MetalPrice.
 * Represents current metal price information.
 */
@Data
@Builder
public class MetalPrice {
    
    private Integer id;
    private MetalType metalType;
    private BigDecimal price;
    private LocalDateTime timestamp;
    
    /**
     * Check if metal price is valid
     */
    public boolean isValid() {
        return metalType != null && 
               price != null && 
               price.compareTo(BigDecimal.ZERO) > 0 &&
               timestamp != null;
    }
    
    /**
     * Check if price is recent (within last hour)
     */
    public boolean isRecent() {
        if (timestamp == null) {
            return false;
        }
        return timestamp.isAfter(LocalDateTime.now().minusHours(1));
    }
    
    /**
     * Calculate value for given amount
     */
    public BigDecimal calculateValue(BigDecimal amount) {
        if (amount == null || price == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(price);
    }
}
