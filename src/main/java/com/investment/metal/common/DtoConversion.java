package com.investment.metal.common;

import com.investment.metal.application.dto.AlertDto;
import com.investment.metal.infrastructure.persistence.entity.Alert;

public final class DtoConversion {

    private DtoConversion() {
    }

    public static AlertDto toDto(Alert alert) {
        return new AlertDto(alert.getId(), alert.getMetalSymbol(), alert.getExpression(), alert.getFrequency().name());
    }

}
