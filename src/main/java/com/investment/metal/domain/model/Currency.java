package com.investment.metal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model for Currency.
 * Represents currency exchange rate information.
 */
@Data
@Builder
public class Currency {
    
    private Integer id;
    private String currencyCode;
    private BigDecimal rateToRon;
    private LocalDateTime lastUpdated;
    
    /**
     * Check if currency is valid
     */
    public boolean isValid() {
        return currencyCode != null && !currencyCode.trim().isEmpty() &&
               rateToRon != null && rateToRon.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Convert amount from this currency to RON
     */
    public BigDecimal convertToRon(BigDecimal amount) {
        if (amount == null || rateToRon == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(rateToRon);
    }
    
    /**
     * Convert amount from RON to this currency
     */
    public BigDecimal convertFromRon(BigDecimal ronAmount) {
        if (ronAmount == null || rateToRon == null || rateToRon.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return ronAmount.divide(rateToRon, 4, java.math.RoundingMode.HALF_UP);
    }
}
