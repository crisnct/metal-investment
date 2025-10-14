package com.investment.metal.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model for Alert.
 * Represents a user's alert configuration for metal price monitoring.
 */
@Data
@Builder
public class Alert {
    
    private Integer id;
    private Integer userId;
    private MetalType metalType;
    private String expression;
    private AlertFrequency frequency;
    private LocalDateTime lastTimeChecked;
    
    /**
     * Business rule: Check if alert configuration is valid
     * An alert is valid if it has all required fields:
     * - userId: Must be specified
     * - metalType: Must be specified
     * - expression: Must be non-empty string
     * - frequency: Must be specified
     * 
     * @return true if alert is valid, false otherwise
     */
    public boolean isValid() {
        return userId != null && 
               metalType != null && 
               expression != null && 
               !expression.trim().isEmpty() &&
               frequency != null;
    }
    
    /**
     * Business rule: Check if alert should be triggered based on frequency
     * Determines if enough time has passed since last check based on alert frequency
     * 
     * @param currentTime the current timestamp to compare against
     * @return true if alert should be checked, false otherwise
     */
    public boolean shouldCheck(LocalDateTime currentTime) {
        if (lastTimeChecked == null) {
            return true; // Never checked before, should check now
        }
        
        return switch (frequency) {
            case HOURLY -> lastTimeChecked.isBefore(currentTime.minusHours(1));
            case DAILY -> lastTimeChecked.isBefore(currentTime.minusDays(1));
            case WEEKLY -> lastTimeChecked.isBefore(currentTime.minusWeeks(1));
            case MONTHLY -> lastTimeChecked.isBefore(currentTime.minusMonths(1));
        };
    }
    
    /**
     * Business rule: Check if alert is overdue for checking
     * An alert is overdue if it should have been checked but wasn't
     * 
     * @param currentTime the current timestamp
     * @return true if alert is overdue, false otherwise
     */
    public boolean isOverdue(LocalDateTime currentTime) {
        if (lastTimeChecked == null) {
            return true; // Never checked, definitely overdue
        }
        
        return switch (frequency) {
            case HOURLY -> lastTimeChecked.isBefore(currentTime.minusHours(2));
            case DAILY -> lastTimeChecked.isBefore(currentTime.minusDays(2));
            case WEEKLY -> lastTimeChecked.isBefore(currentTime.minusWeeks(2));
            case MONTHLY -> lastTimeChecked.isBefore(currentTime.minusMonths(2));
        };
    }
}
