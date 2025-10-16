package com.investment.metal.infrastructure.mapper;

import com.investment.metal.domain.model.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Mapper to bridge domain {@link Currency} objects and persistence entities.
 */
@Component
public class CurrencyMapper {

    public Currency toDomain(com.investment.metal.infrastructure.persistence.entity.Currency entity) {
        if (entity == null) {
            return null;
        }
        Currency.CurrencyBuilder builder = Currency.builder()
                .id(entity.getId())
                .currencyCode(entity.getSymbol())
                .rateToRon(BigDecimal.valueOf(entity.getRon()));

        LocalDateTime lastUpdated = convert(entity.getTime());
        if (lastUpdated != null) {
            builder.lastUpdated(lastUpdated);
        }
        return builder.build();
    }

    public com.investment.metal.infrastructure.persistence.entity.Currency toEntity(Currency currency) {
        if (currency == null) {
            return null;
        }
        com.investment.metal.infrastructure.persistence.entity.Currency entity =
                new com.investment.metal.infrastructure.persistence.entity.Currency();
        entity.setId(currency.getId());
        entity.setSymbol(currency.getCurrencyCode());
        if (currency.getRateToRon() != null) {
            entity.setRon(currency.getRateToRon().doubleValue());
        }
        entity.setTime(convert(currency.getLastUpdated()));
        return entity;
    }

    private LocalDateTime convert(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private java.sql.Timestamp convert(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(dateTime);
    }
}
