package com.investment.metal;

import com.investment.metal.service.CurrencyType;
import com.investment.metal.service.impl.AlertsTrigger;
import com.investment.metal.service.impl.CurrencyService;
import com.investment.metal.service.impl.ExceptionService;
import com.investment.metal.service.impl.MetalPricesService;
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

    @Autowired
    private MetalPricesService metalPricesService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    protected ExceptionService exceptionService;

    @Autowired
    private AlertsTrigger alertsTrigger;

    private final RSSFeedParser rssFeedParser = new RSSFeedParser();

    private MetalType metalType = MetalType.GOLD;

    @PostConstruct
    public void init() {
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
        this.alertsTrigger.trigerAlerts(metalType);

        int ord = (this.metalType.ordinal() + 1) % MetalType.values().length;
        this.metalType = MetalType.values()[ord];
    }

    @Transactional
    @Scheduled(fixedDelay = 12 * 3600 * 1000)
    public void fetchCurrencyValues() {
        for (CurrencyType currency : CurrencyType.values()) {
            if (currency == CurrencyType.USD) {
                this.fetchCurrency(currency, "https://www.bnr.ro/RSS_200004_USD.aspx");
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void fetchCurrency(CurrencyType currency, String feedURL) {
        double ron;
        try {
            ron = this.rssFeedParser.readFeed(feedURL);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.currencyService.save(currency, ron);
    }

}
