package com.investment.metal.application.service;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.repository.PurchaseRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application service for managing metal purchases and sales.
 * Handles business logic for purchasing and selling precious metals.
 * Follows Domain-Driven Design principles by orchestrating domain aggregates via repositories.
 */
@Service
public class PurchaseService {

    private static final int BAD_REQUEST = 400;

    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Record a new metal purchase for a user.
     * If the user already has purchases of this metal type, the amounts and costs are accumulated.
     * 
     * @param userId the ID of the user making the purchase
     * @param metalAmount the amount of metal being purchased
     * @param metalType the type of metal being purchased
     * @param cost the total cost of the purchase
     */
    public void purchase(Integer userId, double metalAmount, MetalType metalType, double cost) {
        MetalPurchase metalPurchase = purchaseRepository
                .findByUserIdAndMetalSymbol(userId, metalType.getSymbol())
                .map(existing -> existing.toBuilder()
                        .amount(existing.getAmount().add(BigDecimal.valueOf(metalAmount)))
                        .cost(existing.getCost().add(BigDecimal.valueOf(cost)))
                        .build())
                .orElseGet(() -> MetalPurchase.builder()
                        .userId(userId)
                        .metalType(metalType)
                        .amount(BigDecimal.valueOf(metalAmount))
                        .cost(BigDecimal.valueOf(cost))
                        .purchaseTime(LocalDateTime.now())
                        .build());

        purchaseRepository.save(metalPurchase);
    }

    /**
     * Record a metal sale for a user.
     * Validates that the user has enough metal to sell and updates the purchase record.
     * Uses proportional cost reduction to maintain accurate cost basis.
     * 
     * @param userId the ID of the user making the sale
     * @param metalAmount the amount of metal being sold
     * @param metalType the type of metal being sold
     * @param price the price per unit at which the metal is being sold
     * @throws BusinessException if the user tries to sell more metal than they own
     */
    public void sell(Integer userId, double metalAmount, MetalType metalType, double price) throws BusinessException {
        MetalPurchase purchase = purchaseRepository
                .findByUserIdAndMetalSymbol(userId, metalType.getSymbol())
                .orElseThrow(() -> new BusinessException(BAD_REQUEST,
                        MessageKey.INVALID_REQUEST.name() + ": purchase not found"));

        if (purchase.getAmount().doubleValue() < metalAmount) {
            throw new BusinessException(BAD_REQUEST,
                    MessageKey.SELL_MORE_THAN_YOU_HAVE.name());
        }

        BigDecimal remainingAmount = purchase.getAmount().subtract(BigDecimal.valueOf(metalAmount));
        BigDecimal costPerUnit = purchase.getCost()
                .divide(purchase.getAmount(), 8, java.math.RoundingMode.HALF_UP);
        BigDecimal costOfSoldAmount = costPerUnit.multiply(BigDecimal.valueOf(metalAmount));
        BigDecimal remainingCost = purchase.getCost().subtract(costOfSoldAmount).max(BigDecimal.ZERO);

        MetalPurchase updated = purchase.toBuilder()
                .amount(remainingAmount)
                .cost(remainingCost)
                .build();

        purchaseRepository.save(updated);
    }

    /**
     * Retrieve all purchases for a specific user.
     * Returns an empty list if the user has no purchases.
     * 
     * @param userId the ID of the user
     * @return list of all metal purchases for the user, or empty list if none found
     */
    public List<MetalPurchase> getAllPurchase(Integer userId) {
        return purchaseRepository.findByUserId(userId);
    }

    /**
     * Retrieve a specific purchase for a user by metal symbol.
     * 
     * @param userId the ID of the user
     * @param metalSymbol the symbol of the metal type
     * @return the metal purchase domain model, or null if not found
     */
    public MetalPurchase getPurchase(Integer userId, String metalSymbol) {
        return purchaseRepository.findByUserIdAndMetalSymbol(userId, metalSymbol).orElse(null);
    }

}
