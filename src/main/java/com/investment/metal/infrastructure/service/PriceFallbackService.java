package com.investment.metal.infrastructure.service;

import com.investment.metal.domain.model.MetalPrice;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.repository.MetalPriceRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Fallback service for providing cached metal prices when external APIs are unavailable.
 * Implements fallback strategy for circuit breaker scenarios.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PriceFallbackService {

    private final MetalPriceRepository metalPriceRepository;

    /**
     * Get the most recent cached price for a metal type.
     * This serves as a fallback when external APIs are unavailable.
     *
     * @param metalType the type of metal to get cached price for
     * @return the most recent cached price, or 0.0 if no cached data available
     */
    public double getCachedPrice(MetalType metalType) {
        return metalPriceRepository.findLatestByMetalType(metalType)
                .map(price -> {
                    double value = price.getPrice().doubleValue();
                    log.info("Using cached price for {}: {}", metalType, value);
                    return value;
                })
                .orElseGet(() -> {
                    log.warn("No cached price data available for {}", metalType);
                    return 0.0;
                });
    }

    /**
     * Check if cached price data is available for a metal type.
     *
     * @param metalType the type of metal to check
     * @return true if cached data is available, false otherwise
     */
    public boolean hasCachedPrice(MetalType metalType) {
        List<MetalPrice> prices = metalPriceRepository.findAllByMetalType(metalType);
        return !prices.isEmpty();
    }

    /**
     * Get the age of the most recent cached price in milliseconds.
     *
     * @param metalType the type of metal to check
     * @return age in milliseconds, or -1 if no cached data available
     */
    public long getCachedPriceAge(MetalType metalType) {
        return metalPriceRepository.findLatestByMetalType(metalType)
                .map(MetalPrice::getTimestamp)
                .map(timestamp -> Duration.between(timestamp, LocalDateTime.now()).toMillis())
                .orElse(-1L);
    }
}
