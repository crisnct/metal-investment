package com.investment.metal.domain.aggregate;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.event.UserRegisteredEvent;
import com.investment.metal.domain.event.UserAuthenticatedEvent;
import com.investment.metal.domain.event.DomainEventPublisher;
import com.investment.metal.domain.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User aggregate root following Domain-Driven Design principles.
 * Encapsulates business operations and maintains consistency boundaries.
 */
public class UserAggregate {
    
    private final User user;
    private final List<Object> domainEvents;
    private final DomainEventPublisher eventPublisher;
    
    public UserAggregate(User user, DomainEventPublisher eventPublisher) {
        this.user = user;
        this.eventPublisher = eventPublisher;
        this.domainEvents = new ArrayList<>();
    }
    
    /**
     * Register a new user
     */
    public static UserAggregate register(String username, String email, DomainEventPublisher eventPublisher) {
        User user = User.builder()
            .username(username)
            .email(email)
            .createdAt(LocalDateTime.now())
            .validated(false)
            .active(true)
            .build();
        
        UserAggregate aggregate = new UserAggregate(user, eventPublisher);
        aggregate.addEvent(new UserRegisteredEvent(user));
        return aggregate;
    }
    
    /**
     * Authenticate user
     */
    public void authenticate(String ipAddress) throws BusinessException {
        if (!user.canBeAuthenticated()) {
            throw new BusinessException(401, "User cannot be authenticated");
        }
        
        addEvent(new UserAuthenticatedEvent(user, ipAddress));
    }
    
    /**
     * Validate user account
     */
    public void validateAccount() {
        if (!user.isValidated()) {
            // Update user validation status
            User validatedUser = user.toBuilder()
                .validated(true)
                .build();
            
            // In a real implementation, you would update the aggregate state
            // For now, we'll just add the event
            addEvent(new UserRegisteredEvent(validatedUser));
        }
    }
    
    /**
     * Get the user domain model
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Get domain events
     */
    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    /**
     * Clear domain events
     */
    public void clearEvents() {
        domainEvents.clear();
    }
    
    private void addEvent(Object event) {
        domainEvents.add(event);
        if (eventPublisher != null) {
            eventPublisher.publish((com.investment.metal.domain.event.DomainEvent) event);
        }
    }
}
