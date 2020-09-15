package com.investment.metal.common;

import com.investment.metal.database.Alert;
import com.investment.metal.dto.AlertDto;

public final class DtoConversion {

    private DtoConversion() {
    }

    public static AlertDto toDto(Alert alert) {
        return new AlertDto(alert.getId(), alert.getMetalSymbol(), alert.getExpression(), alert.getFrequency().name());
    }

}
