package com.investment.metal.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Domain model for Notification.
 * Represents user notification preferences and settings.
 */
@Data
@Builder
public class Notification {
    
    private Integer id;
    private Integer userId;
    private Integer frequency; // days between notifications
    private LocalDateTime lastTimeNotified;
    
    /**
     * Check if notification is valid
     */
    public boolean isValid() {
        return userId != null && 
               frequency != null && 
               frequency > 0;
    }
    
    /**
     * Check if notification should be sent
     */
    public boolean shouldNotify() {
        if (lastTimeNotified == null) {
            return true;
        }
        return lastTimeNotified.isBefore(LocalDateTime.now().minusDays(frequency));
    }
    
    /**
     * Get days since last notification
     */
    public long getDaysSinceLastNotification() {
        if (lastTimeNotified == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(lastTimeNotified, LocalDateTime.now()).toDays();
    }
}
