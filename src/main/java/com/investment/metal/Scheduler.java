package com.investment.metal;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.RSSFeedParser;
import com.investment.metal.service.CurrencyService;
import com.investment.metal.application.service.MetalPriceService;
import com.investment.metal.service.NotificationService;
import com.investment.metal.service.alerts.AlertsTrigger;
import java.io.IOException;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
public class Scheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

  private final MetalPriceService metalPricesService;
  private final CurrencyService currencyService;
  private final AlertsTrigger alertsTrigger;
  private final NotificationService notificationService;
  private final RSSFeedParser rssFeedParser;

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

  @PostConstruct
  public void init() {
    this.fetchCurrencyValues();
    this.updateAllMetalPrices();
  }

  @Transactional
  @Scheduled(fixedDelay = 3600 * 1000)
  public void fetchMetalPrices() {
    this.updateAllMetalPrices();
  }

  private void updateAllMetalPrices() {
    for (MetalType type : MetalType.values()) {
      try {
        final double metalPrice = this.metalPricesService.fetchMetalPrice(type);
        this.metalPricesService.save(type, metalPrice);
        this.alertsTrigger.triggerAlerts(type);
      } catch (Exception e) {
        LOGGER.error("Fail to read metal prices for {}", type, e);
      }
    }
  }

  @Transactional
  @Scheduled(fixedDelay = 12 * 3600 * 1000)
  public void fetchCurrencyValues() {
    try {
      Map<CurrencyType, Double> currenciesValues
          = this.rssFeedParser.readFeed("https://curs.bnr.ro/nbrfxrates.xml");
      currenciesValues.put(CurrencyType.RON, 1.0);
      for (CurrencyType currency : CurrencyType.values()) {
        Double value = currenciesValues.get(currency);
        if (value == null) {
          LOGGER.warn("Currency {} missing from RSS feed", currency);
          continue;
        }
        this.currencyService.save(currency, value);
      }
    } catch (IOException e) {
      LOGGER.error("Fail to read currency values", e);
    }
  }

  @Scheduled(fixedDelay = 3600 * 1000)
  public void checkNotifications() {
    this.notificationService.checkNotifications();
  }

}
