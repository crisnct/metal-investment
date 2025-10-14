package com.investment.metal.application.service;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import com.investment.metal.database.Currency;
import com.investment.metal.database.MetalPrice;
import com.investment.metal.database.MetalPriceRepository;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.UserMetalInfoDto;
import com.investment.metal.price.ExternalMetalPriceReader;
import com.investment.metal.service.CurrencyService;
import com.investment.metal.service.RevolutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Application service for metal price operations.
 * Handles external price fetching and caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetalPriceService {

    public static final long THRESHOLD_TOO_OLD_ENTITIES = TimeUnit.DAYS.toMillis(14);

    private final ExternalMetalPriceReader priceReader;
    private final MetalPriceRepository metalPriceRepository;
    private final RevolutService revolutService;
    private final CurrencyService currencyService;

    /**
     * Get current price for metal type
     */
    public BigDecimal getCurrentPrice(MetalType metalType) {
        try {
            log.debug("Fetching current price for metal: {}", metalType);
            double price = priceReader.fetchPrice(metalType);
            return BigDecimal.valueOf(price);
        } catch (Exception e) {
            log.error("Failed to fetch price for metal: {}", metalType, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Check if price data is available
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
     * Fetch metal price from external service
     */
    public double fetchMetalPrice(MetalType metalType) {
        return priceReader.fetchPrice(metalType);
    }

    /**
     * Get metal price from database
     */
    public MetalPrice getMetalPrice(MetalType metalType) {
        Optional<List<MetalPrice>> price = this.metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
        return price.map(metalPrices -> metalPrices.get(0)).orElse(null);
    }

    /**
     * Get all metal prices for a type
     */
    public Optional<List<MetalPrice>> getMetalPriceAll(MetalType metalType) {
        return this.metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
    }

    /**
     * Calculate user profit for a purchase
     */
    public UserMetalInfoDto calculatesUserProfit(Purchase purchase) {
        CurrencyType currencyType = this.priceReader.getCurrencyType();
        final Currency currency = this.currencyService.findBySymbol(currencyType);
        final double currencyToRonRate = currency.getRon();

        double revolutProfitPercentages = this.revolutService.getRevolutProfitFor(purchase.getMetalType());
        final double metalPriceNowKg = this.priceReader.fetchPrice(purchase.getMetalType());

        double revolutGoldPriceKg = metalPriceNowKg * (revolutProfitPercentages + 1) * currencyToRonRate;
        double revolutGoldPriceOunce = revolutGoldPriceKg * Util.OUNCE;
        double costNowUser = revolutGoldPriceOunce * purchase.getAmount();
        double profitRevolut = costNowUser - purchase.getCost();
        return UserMetalInfoDto
                .builder()
                .metalSymbol(purchase.getMetalSymbol())
                .amountPurchased(purchase.getAmount())
                .costPurchased(purchase.getCost())
                .costNow(costNowUser)
                .profit(profitRevolut)
                .build();
    }

    /**
     * Calculate revolut price for given profit
     */
    public double calculatesRevolutPrice(Purchase purchase, double profit) {
        return (profit + purchase.getCost()) / purchase.getAmount();
    }

    /**
     * Save metal price to database
     */
    public void save(MetalType metalType, double price) {
        final Timestamp timeThreshold = new Timestamp(System.currentTimeMillis() - THRESHOLD_TOO_OLD_ENTITIES);
        final List<MetalPrice> tooOldEntities = this.metalPriceRepository
                .findByMetalSymbol(metalType.getSymbol()).orElse(new ArrayList<>())
                .stream()
                .filter(p -> p.getTime().before(timeThreshold))
                .collect(Collectors.toList());
        if (!tooOldEntities.isEmpty()) {
            this.metalPriceRepository.deleteAll(tooOldEntities);
        }

        final MetalPrice entity = new MetalPrice();
        entity.setMetalSymbol(metalType.getSymbol());
        entity.setPrice(price);
        entity.setTime(new Timestamp(System.currentTimeMillis()));
        this.metalPriceRepository.save(entity);
    }
}
