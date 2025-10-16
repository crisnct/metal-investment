package com.investment.metal.infrastructure.mapper;

import com.investment.metal.domain.model.MetalPrice;
import com.investment.metal.domain.model.MetalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Mapper responsible for translating between domain {@link MetalPrice}
 * aggregates and JPA entities.
 */
@Component
public class MetalPriceMapper {

    public MetalPrice toDomain(com.investment.metal.infrastructure.persistence.entity.MetalPrice entity) {
        if (entity == null) {
            return null;
        }

        return MetalPrice.builder()
                .id(entity.getId())
                .metalType(MetalType.fromSymbol(entity.getMetalSymbol()))
                .price(BigDecimal.valueOf(entity.getPrice()))
                .timestamp(convert(entity.getTime()))
                .build();
    }

    public com.investment.metal.infrastructure.persistence.entity.MetalPrice toEntity(MetalPrice price) {
        if (price == null) {
            return null;
        }
        com.investment.metal.infrastructure.persistence.entity.MetalPrice entity =
                new com.investment.metal.infrastructure.persistence.entity.MetalPrice();
        entity.setId(price.getId());
        entity.setMetalSymbol(price.getMetalType().getSymbol());
        entity.setPrice(price.getPrice().doubleValue());
        entity.setTime(convert(price.getTimestamp()));
        return entity;
    }

    private LocalDateTime convert(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private java.sql.Timestamp convert(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(localDateTime);
    }
}
