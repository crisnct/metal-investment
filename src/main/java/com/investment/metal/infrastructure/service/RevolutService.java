package com.investment.metal.infrastructure.service;

import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.util.Util;
import com.investment.metal.infrastructure.persistence.entity.Currency;
import com.investment.metal.infrastructure.persistence.entity.RevolutProfit;
import com.investment.metal.infrastructure.persistence.repository.RevolutProfitRepository;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.service.price.ExternalMetalPriceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class RevolutService extends AbstractService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RevolutService.class);

    @Autowired
    private RevolutProfitRepository revolutRepository;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExternalMetalPriceReader externalMetalPrice;

    public double calculateRevolutProfit(double revolutPriceOunce, double priceMetalNowKg, MetalType metalType) throws BusinessException {
        CurrencyType currencyType = this.externalMetalPrice.getCurrencyType();
        final Currency currency = currencyService.findBySymbol(currencyType);
        final double currencyToRonRate = currency.getRon();

        double revPrice1KgUSD = revolutPriceOunce / (Util.OUNCE * currencyToRonRate);
        final double profit = (revPrice1KgUSD - priceMetalNowKg) / priceMetalNowKg;

        RevolutProfit revProfit = new RevolutProfit();
        revProfit.setRevolutPriceOz(revolutPriceOunce / currencyToRonRate);
        revProfit.setMetalSymbol(metalType.getSymbol());
        revProfit.setProfit(profit);
        revProfit.setTime(new Timestamp(System.currentTimeMillis()));
        this.revolutRepository.save(revProfit);

        return profit;
    }

    public double getRevolutProfitFor(MetalType metalType) {
        RevolutProfit profit = revolutRepository.findByMetalSymbol(metalType.getSymbol());
        return profit != null ? profit.getProfit() : 0d;
    }
}
