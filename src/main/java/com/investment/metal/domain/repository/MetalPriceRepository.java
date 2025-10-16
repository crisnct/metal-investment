package com.investment.metal.domain.repository;

import com.investment.metal.domain.model.MetalPrice;
import com.investment.metal.domain.model.MetalType;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for {@link MetalPrice}.
 */
public interface MetalPriceRepository {

    Optional<MetalPrice> findLatestByMetalType(MetalType metalType);

    List<MetalPrice> findAllByMetalType(MetalType metalType);

    MetalPrice save(MetalPrice price);

    void deleteAll(List<MetalPrice> prices);
}
