package com.investment.metal.service.impl;

import com.investment.metal.MetalType;
import com.investment.metal.Util;
import com.investment.metal.database.RevolutProfit;
import com.investment.metal.database.RevolutProfitRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.ExternalMetalPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class RevolutService extends AbstractService {

    @Autowired
    private RevolutProfitRepository revolutRepository;

    @Autowired
    private ExternalMetalPriceService externalPriceService;

    public double calculateRevolutProfit(double revolutPriceOunce, double usdRonRate, MetalType metalType) throws BusinessException {
        double priceMetalNowKg = this.externalPriceService.fetchPrice(metalType);
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
