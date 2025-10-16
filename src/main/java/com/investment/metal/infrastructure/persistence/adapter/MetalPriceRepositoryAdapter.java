package com.investment.metal.infrastructure.persistence.adapter;

import com.investment.metal.domain.model.MetalPrice;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.repository.MetalPriceRepository;
import com.investment.metal.infrastructure.mapper.MetalPriceMapper;
import com.investment.metal.infrastructure.persistence.repository.MetalPriceJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Domain repository adapter backed by {@link MetalPriceJpaRepository}.
 */
@Component
public class MetalPriceRepositoryAdapter implements MetalPriceRepository {

    private final MetalPriceJpaRepository metalPriceJpaRepository;
    private final MetalPriceMapper metalPriceMapper;

    public MetalPriceRepositoryAdapter(MetalPriceJpaRepository metalPriceJpaRepository,
                                       MetalPriceMapper metalPriceMapper) {
        this.metalPriceJpaRepository = metalPriceJpaRepository;
        this.metalPriceMapper = metalPriceMapper;
    }

    @Override
    public Optional<MetalPrice> findLatestByMetalType(MetalType metalType) {
        return metalPriceJpaRepository.findByMetalSymbol(metalType.getSymbol())
                .flatMap(list -> list.stream().findFirst())
                .map(metalPriceMapper::toDomain);
    }

    @Override
    public List<MetalPrice> findAllByMetalType(MetalType metalType) {
        return metalPriceJpaRepository.findByMetalSymbol(metalType.getSymbol())
                .orElseGet(List::of)
                .stream()
                .map(metalPriceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public MetalPrice save(MetalPrice price) {
        var saved = metalPriceJpaRepository.save(metalPriceMapper.toEntity(price));
        return metalPriceMapper.toDomain(saved);
    }

    @Override
    public void deleteAll(List<MetalPrice> prices) {
        var entities = prices.stream()
                .map(metalPriceMapper::toEntity)
                .collect(Collectors.toList());
        metalPriceJpaRepository.deleteAll(entities);
    }
}
