package com.investment.metal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

/**
 * Rich domain model for Metal Purchase following Domain-Driven Design principles.
 * Encapsulates business logic for metal purchases and profit calculations.
 */
@Data
@Builder(toBuilder = true)
public class MetalPurchase {
    
    private final Integer id;
    private final Integer userId;
    private final MetalType metalType;
    private final BigDecimal amount;
    private final BigDecimal cost;
    private final LocalDateTime purchaseTime;

    /**
     * Business rule: Purchase amount must be positive
     */
    public boolean isValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Business rule: Purchase cost must be positive
     */
    public boolean isValidCost() {
        return cost != null && cost.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Business rule: Purchase must be valid
     */
    public boolean isValid() {
        return isValidAmount() && isValidCost() && metalType != null;
    }

    /**
     * Calculate current value based on current metal price
     */
    public BigDecimal calculateCurrentValue(BigDecimal currentPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(currentPrice);
    }

    /**
     * Calculate profit/loss based on current metal price
     */
    public BigDecimal calculateProfit(BigDecimal currentPrice) {
        BigDecimal currentValue = calculateCurrentValue(currentPrice);
        return currentValue.subtract(cost);
    }

    /**
     * Calculate profit percentage based on current metal price
     * Formula: ((current_value - cost) / cost) * 100
     * 
     * @param currentPrice the current price per unit of the metal
     * @return profit percentage as BigDecimal, or BigDecimal.ZERO if invalid data
     */
    public BigDecimal calculateProfitPercentage(BigDecimal currentPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0 || 
            cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal profit = calculateProfit(currentPrice);
        return profit.divide(cost, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
    }

    /**
     * Check if purchase is profitable
     */
    public boolean isProfitable(BigDecimal currentPrice) {
        return calculateProfit(currentPrice).compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetalPurchase that = (MetalPurchase) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
