package com.investment.metal;

import com.investment.metal.application.service.AlertsTrigger;
import com.investment.metal.application.service.MetalPriceService;
import com.investment.metal.application.service.NotificationService;
import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.service.CurrencyService;
import com.investment.metal.infrastructure.service.RSSFeedParser;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler configuration for automated tasks.
 * Handles periodic updates of metal prices, currency values, and notifications.
 */
@Configuration
@EnableScheduling
@Slf4j
public class Scheduler {
    
    // Scheduling intervals (in milliseconds)
    private static final long METAL_PRICE_UPDATE_INTERVAL = 3600 * 1000; // 1 hour
    private static final long CURRENCY_UPDATE_INTERVAL = 12 * 3600 * 1000; // 12 hours
    private static final long NOTIFICATION_CHECK_INTERVAL = 3600 * 1000; // 1 hour
    
    // External service URLs
    private static final String BNR_CURRENCY_FEED_URL = "https://curs.bnr.ro/nbrfxrates.xml";

    private final MetalPriceService metalPricesService;
    private final CurrencyService currencyService;
    private final AlertsTrigger alertsTrigger;
    private final NotificationService notificationService;
    private final RSSFeedParser rssFeedParser;

    /**
     * Constructor for Scheduler with dependency injection.
     * 
     * @param metalPricesService service for managing metal prices
     * @param currencyService service for managing currency conversions
     * @param alertsTrigger service for triggering price alerts
     * @param notificationService service for managing notifications
     * @param rssFeedParser service for parsing RSS feeds
     */
    public Scheduler(
            MetalPriceService metalPricesService,
            CurrencyService currencyService,
            AlertsTrigger alertsTrigger,
            NotificationService notificationService,
            RSSFeedParser rssFeedParser) {
        this.metalPricesService = metalPricesService;
        this.currencyService = currencyService;
        this.alertsTrigger = alertsTrigger;
        this.notificationService = notificationService;
        this.rssFeedParser = rssFeedParser;
    }

    /**
     * Initialize scheduler by fetching initial data.
     * Called after dependency injection is complete.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing scheduler - fetching initial data");
        this.fetchCurrencyValues();
        this.updateAllMetalPrices();
    }

    /**
     * Scheduled task to fetch metal prices from external APIs.
     * Runs every hour to keep prices up to date.
     */
    @Transactional
    @Scheduled(fixedDelay = METAL_PRICE_UPDATE_INTERVAL)
    public void fetchMetalPrices() {
        log.info("Starting scheduled metal price update");
        this.updateAllMetalPrices();
    }

    /**
     * Update prices for all metal types.
     * Fetches current prices and triggers alerts if conditions are met.
     */
    private void updateAllMetalPrices() {
        for (MetalType type : MetalType.values()) {
            try {
                double metalPrice = this.metalPricesService.fetchMetalPrice(type);
                this.metalPricesService.save(type, metalPrice);
                this.alertsTrigger.triggerAlerts(type);
                
                log.debug("Successfully updated price for {}: {}", type, metalPrice);
            } catch (Exception e) {
                log.error("Failed to read metal prices for {}", type, e);
            }
        }
    }

    /**
     * Scheduled task to fetch currency exchange rates.
     * Runs every 12 hours to keep currency rates up to date.
     */
    @Transactional
    @Scheduled(fixedDelay = CURRENCY_UPDATE_INTERVAL)
    public void fetchCurrencyValues() {
        log.info("Starting scheduled currency update");
        
        try {
            Map<CurrencyType, Double> currenciesValues = 
                this.rssFeedParser.readFeed(BNR_CURRENCY_FEED_URL);
            
            // Set RON as base currency (1.0)
            currenciesValues.put(CurrencyType.RON, 1.0);
            
            for (CurrencyType currency : CurrencyType.values()) {
                Double value = currenciesValues.get(currency);
                if (value == null) {
                    log.warn("Currency {} missing from RSS feed", currency);
                    continue;
                }
                this.currencyService.save(currency, value);
            }
            
            log.info("Successfully updated currency values");
        } catch (IOException e) {
            log.error("Failed to read currency values from RSS feed", e);
        }
    }

    /**
     * Scheduled task to check and process notifications.
     * Runs every hour to process pending notifications.
     */
    @Scheduled(fixedDelay = NOTIFICATION_CHECK_INTERVAL)
    public void checkNotifications() {
        log.debug("Starting scheduled notification check");
        this.notificationService.checkNotifications();
    }
}