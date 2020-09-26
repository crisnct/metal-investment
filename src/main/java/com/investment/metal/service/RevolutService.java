package com.investment.metal.service;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import com.investment.metal.database.Currency;
import com.investment.metal.database.RevolutProfit;
import com.investment.metal.database.RevolutProfitRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.price.ExternalMetalPriceReader;
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

        double metalPriceOzRON = priceMetalNowKg * Util.OUNCE;

        RevolutProfit revProfit = new RevolutProfit();
        revProfit.setCurrencyToRonRate(currencyToRonRate);
        revProfit.setRevolutPriceOz(revolutPriceOunce / currencyToRonRate);
        revProfit.setMetalPriceOz(metalPriceOzRON);
        revProfit.setMetalSymbol(metalType.getSymbol());
        revProfit.setProfit(profit);
        revProfit.setTime(new Timestamp(System.currentTimeMillis()));
        this.revolutRepository.save(revProfit);

        return profit;
    }

    public double getRevolutProfitFor(MetalType metalType) {
        return revolutRepository.findByMetalSymbol(metalType.getSymbol()).getProfit();
    }
}
