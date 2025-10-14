package com.investment.metal.domain.event;

/**
 * Interface for publishing domain events.
 * Follows Domain-Driven Design principles by enabling decoupled communication.
 */
public interface DomainEventPublisher {
    
    /**
     * Publish a domain event
     */
    void publish(DomainEvent event);
}
