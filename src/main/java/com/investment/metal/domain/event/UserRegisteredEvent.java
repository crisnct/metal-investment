package com.investment.metal.domain.event;

import com.investment.metal.domain.model.User;

/**
 * Domain event fired when a user is registered.
 * Follows Domain-Driven Design principles by enabling decoupled communication.
 */
public class UserRegisteredEvent extends DomainEvent {
    
    private final User user;
    
    public UserRegisteredEvent(User user) {
        super();
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }
}
