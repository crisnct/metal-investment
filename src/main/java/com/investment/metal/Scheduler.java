package com.investment.metal;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.RSSFeedParser;
import com.investment.metal.service.CurrencyService;
import com.investment.metal.service.MetalPriceService;
import com.investment.metal.service.NotificationService;
import com.investment.metal.service.alerts.AlertsTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
        for (CurrencyType currency : CurrencyType.values()) {
            switch (currency) {
                case USD:
                    this.fetchCurrency(currency, "https://www.bnr.ro/RSS_200004_USD.aspx");
                    break;
                case GBP:
                    this.fetchCurrency(currency, "https://www.bnr.ro/RSS_200014_GBP.aspx");
                    break;
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void fetchCurrency(CurrencyType currency, String feedURL) {
        try {
            double ron = this.rssFeedParser.readFeed(feedURL);
            this.currencyService.save(currency, ron);
        } catch (IOException e) {
            LOGGER.error("Fail to read currency value " + currency + " " + feedURL, e);
        }
    }

    @Scheduled(fixedDelay = 3600 * 1000)
    public void checkNotifications() {
        this.notificationService.checkNotifications();
    }

}
