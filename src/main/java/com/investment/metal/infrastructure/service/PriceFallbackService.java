package com.investment.metal.infrastructure.service;

import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.persistence.entity.MetalPrice;
import com.investment.metal.infrastructure.persistence.repository.MetalPriceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback service for providing cached metal prices when external APIs are unavailable.
 * Implements fallback strategy for circuit breaker scenarios.
 */
@Service
@Slf4j
public class PriceFallbackService {

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    /**
     * Get the most recent cached price for a metal type.
     * This serves as a fallback when external APIs are unavailable.
     * 
     * @param metalType the type of metal to get cached price for
     * @return the most recent cached price, or 0.0 if no cached data available
     */
    public double getCachedPrice(MetalType metalType) {
        try {
            Optional<List<MetalPrice>> prices = metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
            
            if (prices.isPresent() && !prices.get().isEmpty()) {
                // Get the most recent price (assuming they are ordered by time)
                MetalPrice latestPrice = prices.get().get(0);
                log.info("Using cached price for {}: {}", metalType, latestPrice.getPrice());
                return latestPrice.getPrice();
            } else {
                log.warn("No cached price data available for {}", metalType);
                return 0.0;
            }
        } catch (Exception e) {
            log.error("Error retrieving cached price for {}: {}", metalType, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Check if cached price data is available for a metal type.
     * 
     * @param metalType the type of metal to check
     * @return true if cached data is available, false otherwise
     */
    public boolean hasCachedPrice(MetalType metalType) {
        try {
            Optional<List<MetalPrice>> prices = metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
            return prices.isPresent() && !prices.get().isEmpty();
        } catch (Exception e) {
            log.error("Error checking cached price availability for {}: {}", metalType, e.getMessage());
            return false;
        }
    }

    /**
     * Get the age of the most recent cached price in milliseconds.
     * 
     * @param metalType the type of metal to check
     * @return age in milliseconds, or -1 if no cached data available
     */
    public long getCachedPriceAge(MetalType metalType) {
        try {
            Optional<List<MetalPrice>> prices = metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
            
            if (prices.isPresent() && !prices.get().isEmpty()) {
                MetalPrice latestPrice = prices.get().get(0);
                long age = System.currentTimeMillis() - latestPrice.getTime().getTime();
                log.debug("Cached price age for {}: {} ms", metalType, age);
                return age;
            }
            return -1;
        } catch (Exception e) {
            log.error("Error calculating cached price age for {}: {}", metalType, e.getMessage());
            return -1;
        }
    }
}
