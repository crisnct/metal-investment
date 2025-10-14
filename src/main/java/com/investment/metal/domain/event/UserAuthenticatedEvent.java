package com.investment.metal.domain.event;

import com.investment.metal.domain.model.User;

/**
 * Domain event fired when a user is authenticated.
 * Follows Domain-Driven Design principles by enabling decoupled communication.
 */
public class UserAuthenticatedEvent extends DomainEvent {
    
    private final User user;
    private final String ipAddress;
    
    public UserAuthenticatedEvent(User user, String ipAddress) {
        super();
        this.user = user;
        this.ipAddress = ipAddress;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
}
