package com.investment.metal.domain.model;

import com.investment.metal.domain.model.AlertFrequency;
import com.investment.metal.domain.model.MetalType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
     * Check if alert is valid
     */
    public boolean isValid() {
        return userId != null && 
               metalType != null && 
               expression != null && 
               !expression.trim().isEmpty() &&
               frequency != null;
    }
    
    /**
     * Check if alert should be triggered based on frequency
     */
    public boolean shouldCheck(LocalDateTime currentTime) {
        if (lastTimeChecked == null) {
            return true;
        }
        
        return switch (frequency) {
            case HOURLY -> lastTimeChecked.isBefore(currentTime.minusHours(1));
            case DAILY -> lastTimeChecked.isBefore(currentTime.minusDays(1));
            case WEEKLY -> lastTimeChecked.isBefore(currentTime.minusWeeks(1));
            case MONTHLY -> lastTimeChecked.isBefore(currentTime.minusMonths(1));
        };
    }
}
