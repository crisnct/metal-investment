package com.investment.metal;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.RSSFeedParser;
import com.investment.metal.service.CurrencyService;
import com.investment.metal.service.MetalPriceService;
import com.investment.metal.service.NotificationService;
import com.investment.metal.service.alerts.AlertsTrigger;
import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
public class Scheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

  private final RSSFeedParser rssFeedParser = new RSSFeedParser();

  @Autowired
  private MetalPriceService metalPricesService;

  @Autowired
  private CurrencyService currencyService;

  @Autowired
  private AlertsTrigger alertsTrigger;

  @Autowired
  private NotificationService notificationService;

  private MetalType metalType = MetalType.GOLD;

  @PostConstruct
  public void init() {
    this.fetchCurrencyValues();
    for (MetalType type : MetalType.values()) {
      this.metalType = type;
      this.fetchMetalPrices();
    }
  }

  @Transactional
  @Scheduled(fixedDelay = 3600 * 1000)
  public void fetchMetalPrices() {
    final double metalPrice = this.metalPricesService.fetchMetalPrice(metalType);
    this.metalPricesService.save(metalType, metalPrice);
    this.alertsTrigger.triggerAlerts(metalType);

    int ord = (this.metalType.ordinal() + 1) % MetalType.values().length;
    this.metalType = MetalType.values()[ord];
  }

  @Transactional
  @Scheduled(fixedDelay = 12 * 3600 * 1000)
  public void fetchCurrencyValues() {
    try {
      Map<CurrencyType, Double> currenciesValues
          = this.rssFeedParser.readFeed("https://curs.bnr.ro/nbrfxrates.xml");
      currenciesValues.put(CurrencyType.RON, 1.0);
      for (CurrencyType currency : CurrencyType.values()) {
        this.currencyService.save(currency, currenciesValues.get(currency));
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
