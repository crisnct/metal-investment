package com.investment.metal.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for domain events.
 * Follows Domain-Driven Design principles by enabling decoupled communication.
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
