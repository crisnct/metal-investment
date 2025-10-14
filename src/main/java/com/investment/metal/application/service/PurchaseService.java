package com.investment.metal.application.service;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import com.investment.metal.infrastructure.persistence.repository.PurchaseRepository;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.mapper.MetalPurchaseMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Application service for managing metal purchases and sales.
 * Handles business logic for purchasing and selling precious metals.
 * Follows Clean Architecture principles by orchestrating domain and infrastructure concerns.
 */
@Service
public class PurchaseService {

    /**
     * Repository for managing purchase data persistence
     */
    @Autowired
    private PurchaseRepository purchaseRepo;

    /**
     * Exception service for handling business exceptions
     */
    @Autowired
    private ExceptionService exceptionService;

    /**
     * Mapper for converting between domain models and infrastructure entities
     */
    @Autowired
    private MetalPurchaseMapper metalPurchaseMapper;

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
        Optional<Purchase> purchaseOp = this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalType.getSymbol());
        final MetalPurchase metalPurchase;
        if (purchaseOp.isPresent()) {
            // User already has purchases of this metal type - accumulate amounts and costs
            MetalPurchase existingPurchase = metalPurchaseMapper.toDomainModel(purchaseOp.get());
            metalPurchase = existingPurchase.toBuilder()
                    .amount(existingPurchase.getAmount().add(BigDecimal.valueOf(metalAmount)))
                    .cost(existingPurchase.getCost().add(BigDecimal.valueOf(cost)))
                    .build();
        } else {
            // First purchase of this metal type for the user
            metalPurchase = MetalPurchase.builder()
                    .userId(userId)
                    .metalType(metalType)
                    .amount(BigDecimal.valueOf(metalAmount))
                    .cost(BigDecimal.valueOf(cost))
                    .purchaseTime(java.time.LocalDateTime.now())
                    .build();
        }
        
        // Convert domain model to entity and save
        Purchase purchaseEntity = metalPurchaseMapper.toEntity(metalPurchase);
        this.purchaseRepo.save(purchaseEntity);
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
        @SuppressWarnings("OptionalGetWithoutIsPresent") final Purchase purchase = this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalType.getSymbol()).get();
        
        // Business rule: User cannot sell more metal than they own
        this.exceptionService.check(metalAmount > purchase.getAmount(), MessageKey.SELL_MORE_THAN_YOU_HAVE, metalType.getSymbol(), purchase.getAmount());

        double amount = purchase.getAmount();
        double newAmount = amount - metalAmount;

        // Calculate the proportional cost reduction based on the amount being sold
        // This maintains accurate cost basis for remaining holdings
        double costPerUnit = purchase.getCost() / purchase.getAmount();
        double costOfSoldAmount = costPerUnit * metalAmount;
        double newCost = purchase.getCost() - costOfSoldAmount;
        
        // Ensure cost never goes negative (safety check)
        if (newCost < 0) {
            newCost = 0;
        }
        
        // Update the purchase record with new amounts
        purchase.setAmount(newAmount);
        purchase.setCost(newCost);
        this.purchaseRepo.save(purchase);
    }

    /**
     * Retrieve all purchases for a specific user.
     * Returns an empty list if the user has no purchases.
     * 
     * @param userId the ID of the user
     * @return list of all metal purchases for the user, or empty list if none found
     */
    public List<MetalPurchase> getAllPurchase(Integer userId) {
        List<Purchase> purchaseEntities = this.purchaseRepo.findByUserId(userId).orElse(new ArrayList<>());
        return purchaseEntities.stream()
                .map(metalPurchaseMapper::toDomainModel)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieve a specific purchase for a user by metal symbol.
     * 
     * @param userId the ID of the user
     * @param metalSymbol the symbol of the metal type
     * @return the metal purchase domain model, or null if not found
     */
    public MetalPurchase getPurchase(Integer userId, String metalSymbol) {
        Optional<Purchase> purchaseEntity = this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalSymbol);
        return purchaseEntity.map(metalPurchaseMapper::toDomainModel).orElse(null);
    }

}
