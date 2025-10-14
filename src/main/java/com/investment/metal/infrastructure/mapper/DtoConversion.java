package com.investment.metal.infrastructure.mapper;

import com.investment.metal.application.dto.AlertDto;
import com.investment.metal.infrastructure.persistence.entity.Alert;

/**
 * Infrastructure mapper for converting between DTOs and entities.
 * Follows Clean Architecture principles by keeping mapping logic in infrastructure layer.
 */
public final class DtoConversion {

    private DtoConversion() {
    }

    public static AlertDto toDto(Alert alert) {
        return new AlertDto(alert.getId(), alert.getMetalSymbol(), alert.getExpression(), alert.getFrequency().name());
    }
}
