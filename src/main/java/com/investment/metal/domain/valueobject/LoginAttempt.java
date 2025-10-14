package com.investment.metal.domain.valueobject;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Value object for login attempts.
 * Immutable and encapsulates login attempt information.
 */
@Data
@Builder
public class LoginAttempt {
    
    private final Integer userId;
    private final String ipAddress;
    private final LocalDateTime timestamp;
    private final boolean successful;
    private final String failureReason;
    private final int attemptCount;

    /**
     * Create successful login attempt
     */
    public static LoginAttempt successful(Integer userId, String ipAddress) {
        return LoginAttempt.builder()
            .userId(userId)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .successful(true)
            .attemptCount(0)
            .build();
    }

    /**
     * Create failed login attempt
     */
    public static LoginAttempt failed(Integer userId, String ipAddress, String reason, int attemptCount) {
        return LoginAttempt.builder()
            .userId(userId)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .successful(false)
            .failureReason(reason)
            .attemptCount(attemptCount)
            .build();
    }

    /**
     * Check if this is a repeated failed attempt
     */
    public boolean isRepeatedFailure() {
        return !successful && attemptCount > 1;
    }

    /**
     * Check if this attempt should trigger security measures
     */
    public boolean shouldTriggerSecurity() {
        return !successful && attemptCount >= 3;
    }
}
