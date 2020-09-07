package com.investment.metal;

import com.investment.metal.database.MetalPrice;
import com.investment.metal.service.ExternalMetalPriceService;
import com.investment.metal.service.MetalPricesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

@Configuration
@EnableScheduling
public class SchedulerMetalPrices {

    @Autowired
    private ExternalMetalPriceService externalPriceService;

    @Autowired
    private MetalPricesService metalPricesService;

    private MetalType metalType = MetalType.GOLD;

    @PostConstruct
    public void init() {
        for (MetalType type : MetalType.values()) {
            this.metalType = type;
            this.fetchMetalPrices();
        }
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10 * 60 * 1000)
    public void fetchMetalPrices() {
        final double metalPrice = this.externalPriceService.fetchPrice(metalType);

        MetalPrice price = new MetalPrice();
        price.setMetalSymbol(metalType.getSymbol());
        price.setPrice(metalPrice);
        price.setTime(new Timestamp(System.currentTimeMillis()));
        this.metalPricesService.save(price);

        int ord = (this.metalType.ordinal() + 1) % MetalType.values().length;
        this.metalType = MetalType.values()[ord];
    }


}
