package com.investment.metal.domain.service;

import com.investment.metal.domain.model.Alert;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.exception.BusinessException;
import java.util.List;

/**
 * Domain service interface for alert business operations.
 * Defines the contract for alert business logic without infrastructure dependencies.
 * Follows DDD principles by keeping domain services in the domain layer.
 */
public interface AlertDomainService {

    /**
     * Create a new price alert with business validation.
     * 
     * @param userId the user creating the alert
     * @param metalType the metal type to monitor
     * @param expression the mathematical expression for the alert
     * @return the created alert
     * @throws BusinessException if alert creation fails
     */
    Alert createAlert(Integer userId, MetalType metalType, String expression) throws BusinessException;

    /**
     * Update an existing alert.
     * 
     * @param alertId the alert ID to update
     * @param expression the new expression
     * @return the updated alert
     * @throws BusinessException if update fails
     */
    Alert updateAlert(Integer alertId, String expression) throws BusinessException;

    /**
     * Delete an alert.
     * 
     * @param alertId the alert ID to delete
     * @throws BusinessException if deletion fails
     */
    void deleteAlert(Integer alertId) throws BusinessException;

    /**
     * Get all alerts for a user.
     * 
     * @param userId the user ID
     * @return list of user's alerts
     * @throws BusinessException if retrieval fails
     */
    List<Alert> getUserAlerts(Integer userId) throws BusinessException;

    /**
     * Get alerts by metal type for a user.
     * 
     * @param userId the user ID
     * @param metalType the metal type to filter by
     * @return list of alerts for the metal type
     * @throws BusinessException if retrieval fails
     */
    List<Alert> getUserAlertsByMetalType(Integer userId, MetalType metalType) throws BusinessException;

    /**
     * Check if an alert condition is met.
     * 
     * @param alert the alert to check
     * @param currentPrice the current metal price
     * @return true if alert condition is met
     * @throws BusinessException if evaluation fails
     */
    boolean isAlertConditionMet(Alert alert, double currentPrice) throws BusinessException;
}
