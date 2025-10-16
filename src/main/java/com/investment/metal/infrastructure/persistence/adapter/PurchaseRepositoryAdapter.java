package com.investment.metal.infrastructure.persistence.adapter;

import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.repository.PurchaseRepository;
import com.investment.metal.infrastructure.mapper.MetalPurchaseMapper;
import com.investment.metal.infrastructure.persistence.repository.PurchaseJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter that bridges the domain {@link PurchaseRepository}
 * with the Spring Data JPA implementation.
 */
@Component
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;
    private final MetalPurchaseMapper metalPurchaseMapper;

    public PurchaseRepositoryAdapter(
            PurchaseJpaRepository purchaseJpaRepository,
            MetalPurchaseMapper metalPurchaseMapper) {
        this.purchaseJpaRepository = purchaseJpaRepository;
        this.metalPurchaseMapper = metalPurchaseMapper;
    }

    @Override
    public Optional<MetalPurchase> findByUserIdAndMetalSymbol(Integer userId, String metalSymbol) {
        return purchaseJpaRepository.findByUserIdAndMetalSymbol(userId, metalSymbol)
                .map(metalPurchaseMapper::toDomainModel);
    }

    @Override
    public List<MetalPurchase> findByUserId(Integer userId) {
        return purchaseJpaRepository.findByUserId(userId)
                .orElseGet(List::of)
                .stream()
                .map(metalPurchaseMapper::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public MetalPurchase save(MetalPurchase purchase) {
        var saved = purchaseJpaRepository.save(metalPurchaseMapper.toEntity(purchase));
        return metalPurchaseMapper.toDomainModel(saved);
    }

    @Override
    public void deleteByUserIdAndMetalSymbol(Integer userId, String metalSymbol) {
        purchaseJpaRepository.deleteByUserIdAndMetalSymbol(userId, metalSymbol);
    }
}
