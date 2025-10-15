package com.investment.metal.infrastructure.service;

import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.service.price.ExternalMetalPriceReader;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Resilient wrapper service for external price API calls.
 * Implements circuit breaker pattern to handle external API failures gracefully.
 */
@Service
public class ResilientPriceService {

    private static final Logger logger = LoggerFactory.getLogger(ResilientPriceService.class);
    private static final String CIRCUIT_BREAKER_NAME = "price-api";

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ExternalMetalPriceReader priceReader;

    @Autowired
    private PriceFallbackService fallbackService;

    /**
     * Fetch price with circuit breaker protection.
     * 
     * @param metalType the type of metal to fetch price for
     * @return the current price of the metal
     * @throws RuntimeException if the circuit breaker is open or API call fails
     */
    public double fetchPrice(MetalType metalType) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        
        logger.info("Fetching price for {} with circuit breaker state: {}", 
                   metalType, circuitBreaker.getState());

        try {
            return circuitBreaker.executeSupplier(() -> {
                try {
                    double price = priceReader.fetchPrice(metalType);
                    logger.info("Successfully fetched price for {}: {}", metalType, price);
                    return price;
                } catch (Exception e) {
                    logger.error("Failed to fetch price for {}: {}", metalType, e.getMessage());
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.warn("Circuit breaker protection triggered for {}. Attempting fallback to cached price.", metalType);
            return getFallbackPrice(metalType);
        }
    }

    /**
     * Get fallback price from cache when external API is unavailable.
     * 
     * @param metalType the type of metal to get fallback price for
     * @return cached price or 0.0 if no cached data available
     */
    private double getFallbackPrice(MetalType metalType) {
        if (fallbackService.hasCachedPrice(metalType)) {
            double cachedPrice = fallbackService.getCachedPrice(metalType);
            long age = fallbackService.getCachedPriceAge(metalType);
            logger.info("Using fallback cached price for {}: {} (age: {} ms)", 
                       metalType, cachedPrice, age);
            return cachedPrice;
        } else {
            logger.error("No fallback price available for {}", metalType);
            return 0.0;
        }
    }

    /**
     * Fetch price asynchronously with circuit breaker protection.
     * 
     * @param metalType the type of metal to fetch price for
     * @return CompletableFuture containing the price
     */
    public CompletableFuture<Double> fetchPriceAsync(MetalType metalType) {
        return CompletableFuture.supplyAsync(() -> fetchPrice(metalType));
    }

    /**
     * Get the currency type from the underlying price reader.
     * 
     * @return the currency type
     */
    public CurrencyType getCurrencyType() {
        return priceReader.getCurrencyType();
    }

    /**
     * Check if the circuit breaker is currently open.
     * 
     * @return true if circuit breaker is open, false otherwise
     */
    public boolean isCircuitBreakerOpen() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }

    /**
     * Get the current circuit breaker state.
     * 
     * @return the current state of the circuit breaker
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        return circuitBreaker.getState();
    }
}
