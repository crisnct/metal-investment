package com.investment.metal.application.service;

import com.investment.metal.application.dto.UserMetalInfoDto;
import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalPrice;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.repository.MetalPriceRepository;
import com.investment.metal.infrastructure.persistence.entity.Currency;
import com.investment.metal.infrastructure.service.CurrencyService;
import com.investment.metal.infrastructure.service.ResilientPriceService;
import com.investment.metal.infrastructure.service.RevolutService;
import com.investment.metal.infrastructure.util.Util;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application service for metal price operations.
 * Handles external price fetching, caching, and profit calculations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetalPriceService {

    // Threshold for cleaning up old price entries (14 days)
    public static final long THRESHOLD_TOO_OLD_ENTITIES = TimeUnit.DAYS.toMillis(14);

    private final ResilientPriceService resilientPriceService;
    private final MetalPriceRepository metalPriceRepository;
    private final RevolutService revolutService;
    private final CurrencyService currencyService;

    /**
     * Get current price for metal type from external API.
     * 
     * @param metalType the type of metal to get price for
     * @return the current price or BigDecimal.ZERO if fetch fails
     */
    public BigDecimal getCurrentPrice(MetalType metalType) {
        try {
            log.debug("Fetching current price for metal: {} with circuit breaker protection", metalType);
            double price = resilientPriceService.fetchPrice(metalType);
            return BigDecimal.valueOf(price);
        } catch (Exception e) {
            log.error("Failed to fetch price for metal: {} - Circuit breaker state: {}", 
                     metalType, resilientPriceService.getCircuitBreakerState(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Check if price data is available for the specified metal type.
     * 
     * @param metalType the type of metal to check
     * @return true if price data is available and greater than zero
     */
    public boolean isPriceDataAvailable(MetalType metalType) {
        try {
            BigDecimal price = getCurrentPrice(metalType);
            return price != null && price.compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception e) {
            log.warn("Price data not available for metal: {}", metalType, e);
            return false;
        }
    }

    /**
     * Fetch metal price from external service.
     * 
     * @param metalType the type of metal to fetch price for
     * @return the current price from external API
     * @throws RuntimeException if the API call fails
     */
    public double fetchMetalPrice(MetalType metalType) {
        return resilientPriceService.fetchPrice(metalType);
    }

    /**
     * Get the latest metal price from database.
     * 
     * @param metalType the type of metal to get price for
     * @return the latest metal price entity or null if not found
     */
    public MetalPrice getMetalPrice(MetalType metalType) {
        return this.metalPriceRepository.findLatestByMetalType(metalType).orElse(null);
    }

    /**
     * Get all metal prices for a specific metal type.
     * 
     * @param metalType the type of metal to get prices for
     * @return optional list of metal prices
     */
    public Optional<List<MetalPrice>> getMetalPriceAll(MetalType metalType) {
        List<MetalPrice> prices = this.metalPriceRepository.findAllByMetalType(metalType);
        return prices.isEmpty() ? Optional.empty() : Optional.of(prices);
    }

    /**
     * Calculate user profit for a purchase based on current market prices.
     * 
     * @param purchase the purchase to calculate profit for
     * @return user metal info DTO with profit calculations
     */
    public UserMetalInfoDto calculatesUserProfit(MetalPurchase purchase) {
        CurrencyType currencyType = this.resilientPriceService.getCurrencyType();
        Currency currency = this.currencyService.findBySymbol(currencyType);
        double currencyToRonRate = currency.getRon();

        double revolutProfitPercentages = this.revolutService
            .getRevolutProfitFor(purchase.getMetalType());
        double metalPriceNowKg = this.resilientPriceService.fetchPrice(purchase.getMetalType());

        // Calculate current value with Revolut markup
        double revolutGoldPriceKg = metalPriceNowKg * (revolutProfitPercentages + 1) * currencyToRonRate;
        double revolutGoldPriceOunce = revolutGoldPriceKg * Util.OUNCE;
        double amountPurchased = purchase.getAmount().doubleValue();
        double costNowUser = revolutGoldPriceOunce * amountPurchased;
        double profitRevolut = costNowUser - purchase.getCost().doubleValue();
        
        return UserMetalInfoDto
                .builder()
                .metalSymbol(purchase.getMetalType().getSymbol())
                .amountPurchased(amountPurchased)
                .costPurchased(purchase.getCost().doubleValue())
                .costNow(costNowUser)
                .profit(profitRevolut)
                .build();
    }

    /**
     * Calculate Revolut price for given profit target.
     * 
     * @param purchase the purchase to calculate price for
     * @param profit the desired profit amount
     * @return the calculated Revolut price per unit
     */
    public double calculatesRevolutPrice(MetalPurchase purchase, double profit) {
        double cost = purchase.getCost().doubleValue();
        double amount = purchase.getAmount().doubleValue();
        return (profit + cost) / amount;
    }

    /**
     * Save metal price to database with automatic cleanup of old entries.
     * 
     * @param metalType the type of metal
     * @param price the price to save
     */
    public void save(MetalType metalType, double price) {
        LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofMillis(THRESHOLD_TOO_OLD_ENTITIES));

        List<MetalPrice> existingPrices = this.metalPriceRepository.findAllByMetalType(metalType);
        List<MetalPrice> tooOldEntities = existingPrices.stream()
                .filter(p -> p.getTimestamp() != null && p.getTimestamp().isBefore(cutoff))
                .collect(Collectors.toList());

        if (!tooOldEntities.isEmpty()) {
            this.metalPriceRepository.deleteAll(tooOldEntities);
            log.debug("Cleaned up {} old price entries for {}",
                    tooOldEntities.size(), metalType);
        }

        MetalPrice metalPrice = MetalPrice.builder()
                .metalType(metalType)
                .price(BigDecimal.valueOf(price))
                .timestamp(LocalDateTime.now())
                .build();

        this.metalPriceRepository.save(metalPrice);
        
        log.debug("Saved price for {}: {}", metalType, price);
    }
}
