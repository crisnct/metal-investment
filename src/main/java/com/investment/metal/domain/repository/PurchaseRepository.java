package com.investment.metal.domain.repository;

import com.investment.metal.domain.model.MetalPurchase;
import java.util.List;
import java.util.Optional;

/**
 * Domain-level repository abstraction for accessing {@link MetalPurchase} aggregates.
 * Application services depend on this interface rather than infrastructure-specific implementations.
 */
public interface PurchaseRepository {

    Optional<MetalPurchase> findByUserIdAndMetalSymbol(Integer userId, String metalSymbol);

    List<MetalPurchase> findByUserId(Integer userId);

    MetalPurchase save(MetalPurchase purchase);

    void deleteByUserIdAndMetalSymbol(Integer userId, String metalSymbol);
}
