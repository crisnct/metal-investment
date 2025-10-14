package com.investment.metal.application.repository.impl;

import com.investment.metal.application.repository.MetalPurchaseRepository;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import com.investment.metal.infrastructure.persistence.repository.PurchaseRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Implementation of MetalPurchaseRepository.
 * Bridges between domain layer and infrastructure layer.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MetalPurchaseRepositoryImpl implements MetalPurchaseRepository {

    private final PurchaseRepository purchaseRepository;

    @Override
    public List<MetalPurchase> findByUserId(Integer userId) {
        try {
            Optional<List<Purchase>> purchasesOpt = purchaseRepository.findByUserId(userId);
            if (purchasesOpt.isEmpty()) {
                return List.of();
            }
            return purchasesOpt.get().stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to find purchases for user: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<MetalPurchase> findByUserIdAndMetalType(Integer userId, String metalSymbol) {
        try {
            Optional<Purchase> purchaseOpt = purchaseRepository.findByUserIdAndMetalSymbol(userId, metalSymbol);
            if (purchaseOpt.isEmpty()) {
                return List.of();
            }
            return List.of(toDomainModel(purchaseOpt.get()));
        } catch (Exception e) {
            log.warn("Failed to find purchases for user: {} and metal: {}", userId, metalSymbol, e);
            return List.of();
        }
    }

    @Override
    public MetalPurchase save(MetalPurchase metalPurchase) {
        try {
            Purchase purchase = toEntity(metalPurchase);
            Purchase savedPurchase = purchaseRepository.save(purchase);
            return toDomainModel(savedPurchase);
        } catch (Exception e) {
            log.error("Failed to save metal purchase for user: {}", metalPurchase.getUserId(), e);
            throw new RuntimeException("Failed to save metal purchase", e);
        }
    }

    @Override
    public void deleteByUserIdAndMetalType(Integer userId, String metalSymbol) {
        try {
            purchaseRepository.deleteByUserIdAndMetalSymbol(userId, metalSymbol);
        } catch (Exception e) {
            log.error("Failed to delete purchases for user: {} and metal: {}", userId, metalSymbol, e);
            throw new RuntimeException("Failed to delete purchases", e);
        }
    }

    @Override
    public Optional<MetalPurchase> findById(Integer id) {
        try {
            Optional<Purchase> purchaseOpt = purchaseRepository.findById(id);
            if (purchaseOpt.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(toDomainModel(purchaseOpt.get()));
        } catch (Exception e) {
            log.warn("Failed to find purchase by id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Integer id) {
        try {
            purchaseRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete purchase by id: {}", id, e);
            throw new RuntimeException("Failed to delete purchase", e);
        }
    }

    @Override
    public long countByUserId(Integer userId) {
        try {
            Optional<List<Purchase>> purchasesOpt = purchaseRepository.findByUserId(userId);
            return purchasesOpt.map(List::size).orElse(0);
        } catch (Exception e) {
            log.warn("Failed to count purchases for user: {}", userId, e);
            return 0;
        }
    }

    private MetalPurchase toDomainModel(Purchase purchase) {
        return MetalPurchase.builder()
            .id(purchase.getId())
            .userId(purchase.getUserId())
            .metalType(purchase.getMetalType())
            .amount(java.math.BigDecimal.valueOf(purchase.getAmount()))
            .cost(java.math.BigDecimal.valueOf(purchase.getCost()))
            .purchaseTime(purchase.getTime().toLocalDateTime())
            .build();
    }

    private Purchase toEntity(MetalPurchase metalPurchase) {
        Purchase purchase = new Purchase();
        purchase.setUserId(metalPurchase.getUserId());
        purchase.setMetalSymbol(metalPurchase.getMetalType().getSymbol());
        purchase.setAmount(metalPurchase.getAmount().doubleValue());
        purchase.setCost(metalPurchase.getCost().doubleValue());
        purchase.setTime(java.sql.Timestamp.valueOf(metalPurchase.getPurchaseTime()));
        return purchase;
    }
}
