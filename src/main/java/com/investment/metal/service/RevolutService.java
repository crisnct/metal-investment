package com.investment.metal.service;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import com.investment.metal.database.Currency;
import com.investment.metal.database.RevolutProfit;
import com.investment.metal.database.RevolutProfitRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class RevolutService extends AbstractService {

    @Autowired
    private RevolutProfitRepository revolutRepository;

    @Autowired
    private CurrencyService currencyService;

    public double calculateRevolutProfit(double revolutPriceOunce, double priceMetalNowKg, MetalType metalType) throws BusinessException {
        final Currency currency = currencyService.findBySymbol(CurrencyType.USD);
        final double usdRonRate = currency.getRon();

        double diffCostKg = revolutPriceOunce / (Util.ounce * usdRonRate) - priceMetalNowKg;
        final double profit = diffCostKg / priceMetalNowKg;

        RevolutProfit revProfit = this.revolutRepository.findByMetalSymbol(metalType.getSymbol());
        if (revProfit == null) {
            revProfit = new RevolutProfit();
        }
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
