package com.investment.metal.infrastructure.mapper;

import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Infrastructure mapper for converting between MetalPurchase domain model and Purchase entity.
 * Follows Clean Architecture principles by keeping mapping logic in infrastructure layer.
 */
@Component
public class MetalPurchaseMapper {

    /**
     * Convert Purchase entity to MetalPurchase domain model.
     * 
     * @param purchase the JPA entity
     * @return the domain model
     */
    public MetalPurchase toDomainModel(Purchase purchase) {
        if (purchase == null) {
            return null;
        }
        
        return MetalPurchase.builder()
                .id(purchase.getId())
                .userId(purchase.getUserId())
                .metalType(MetalType.fromSymbol(purchase.getMetalSymbol()))
                .amount(BigDecimal.valueOf(purchase.getAmount()))
                .cost(BigDecimal.valueOf(purchase.getCost()))
                .purchaseTime(convertToLocalDateTime(purchase.getTime()))
                .build();
    }

    /**
     * Convert MetalPurchase domain model to Purchase entity.
     * 
     * @param metalPurchase the domain model
     * @return the JPA entity
     */
    public Purchase toEntity(MetalPurchase metalPurchase) {
        if (metalPurchase == null) {
            return null;
        }
        
        Purchase purchase = new Purchase();
        purchase.setId(metalPurchase.getId());
        purchase.setUserId(metalPurchase.getUserId());
        purchase.setMetalSymbol(metalPurchase.getMetalType().getSymbol());
        purchase.setAmount(metalPurchase.getAmount().doubleValue());
        purchase.setCost(metalPurchase.getCost().doubleValue());
        purchase.setTime(convertToTimestamp(metalPurchase.getPurchaseTime()));
        return purchase;
    }

    /**
     * Update Purchase entity with MetalPurchase domain model data.
     * 
     * @param purchase the existing entity to update
     * @param metalPurchase the domain model with new data
     */
    public void updateEntity(Purchase purchase, MetalPurchase metalPurchase) {
        if (purchase == null || metalPurchase == null) {
            return;
        }
        
        purchase.setUserId(metalPurchase.getUserId());
        purchase.setMetalSymbol(metalPurchase.getMetalType().getSymbol());
        purchase.setAmount(metalPurchase.getAmount().doubleValue());
        purchase.setCost(metalPurchase.getCost().doubleValue());
        purchase.setTime(convertToTimestamp(metalPurchase.getPurchaseTime()));
    }

    /**
     * Convert java.sql.Timestamp to LocalDateTime.
     */
    private LocalDateTime convertToLocalDateTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to java.sql.Timestamp.
     */
    private java.sql.Timestamp convertToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(localDateTime);
    }
}
