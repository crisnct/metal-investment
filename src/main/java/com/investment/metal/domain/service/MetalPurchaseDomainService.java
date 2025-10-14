package com.investment.metal.domain.service;

import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service interface for metal purchase business operations.
 * Defines the contract for metal purchase business logic without infrastructure dependencies.
 * Follows DDD principles by keeping domain services in the domain layer.
 */
public interface MetalPurchaseDomainService {

    /**
     * Create a new metal purchase with business validation.
     * 
     * @param userId the user making the purchase
     * @param metalType the type of metal being purchased
     * @param amount the amount of metal to purchase
     * @param cost the cost of the purchase
     * @return the created metal purchase
     * @throws BusinessException if purchase creation fails
     */
    MetalPurchase createPurchase(Integer userId, MetalType metalType, BigDecimal amount, BigDecimal cost) throws BusinessException;

    /**
     * Sell metal and calculate profit/loss.
     * 
     * @param purchase the purchase to sell
     * @param currentPrice the current market price
     * @return the profit/loss from the sale
     * @throws BusinessException if sale fails
     */
    BigDecimal sellMetal(MetalPurchase purchase, BigDecimal currentPrice) throws BusinessException;

    /**
     * Get all purchases for a user.
     * 
     * @param userId the user ID
     * @return list of user's purchases
     * @throws BusinessException if retrieval fails
     */
    List<MetalPurchase> getUserPurchases(Integer userId) throws BusinessException;

    /**
     * Get purchases by metal type for a user.
     * 
     * @param userId the user ID
     * @param metalType the metal type to filter by
     * @return list of purchases for the metal type
     * @throws BusinessException if retrieval fails
     */
    List<MetalPurchase> getUserPurchasesByMetalType(Integer userId, MetalType metalType) throws BusinessException;

    /**
     * Calculate total profit for a user's metal type.
     * 
     * @param userId the user ID
     * @param metalType the metal type
     * @param currentPrice the current market price
     * @return the total profit/loss
     * @throws BusinessException if calculation fails
     */
    BigDecimal calculateTotalProfit(Integer userId, MetalType metalType, BigDecimal currentPrice) throws BusinessException;
}
