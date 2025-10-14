package com.investment.metal.infrastructure.mapper;

import com.investment.metal.application.dto.AlertDto;
import com.investment.metal.infrastructure.persistence.entity.Alert;
import org.springframework.stereotype.Component;

/**
 * Infrastructure mapper for converting between DTOs and entities. Follows Clean Architecture principles by keeping mapping logic in infrastructure
 * layer.
 */
@Component
public final class DtoMapper {

  public AlertDto toDto(Alert alert) {
    return new AlertDto(alert.getId(), alert.getMetalSymbol(), alert.getExpression(), alert.getFrequency().name());
  }
}
