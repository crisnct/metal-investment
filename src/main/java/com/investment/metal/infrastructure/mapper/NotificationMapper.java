package com.investment.metal.infrastructure.mapper;

import com.investment.metal.domain.model.Notification;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Mapper translating between domain {@link Notification} aggregates and JPA entities.
 */
@Component
public class NotificationMapper {

    public Notification toDomain(com.investment.metal.infrastructure.persistence.entity.Notification entity) {
        if (entity == null) {
            return null;
        }
        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .frequency(entity.getFrequency())
                .lastTimeNotified(convert(entity.getLastTimeNotified()))
                .build();
    }

    public com.investment.metal.infrastructure.persistence.entity.Notification toEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        com.investment.metal.infrastructure.persistence.entity.Notification entity =
                new com.investment.metal.infrastructure.persistence.entity.Notification();
        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId());
        if (notification.getFrequency() != null) {
            entity.setFrequency(notification.getFrequency());
        }
        entity.setLastTimeNotified(convert(notification.getLastTimeNotified()));
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
