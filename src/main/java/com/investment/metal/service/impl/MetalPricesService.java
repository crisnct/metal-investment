package com.investment.metal.service.impl;

import com.investment.metal.MetalType;
import com.investment.metal.Util;
import com.investment.metal.database.MetalPrice;
import com.investment.metal.database.MetalPriceRepository;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.MetalInfo;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetalPricesService extends AbstractService {

    //TODO save this in the database and make a scheduler to update it from https://www.bnr.ro/RSS_200004_USD.aspx
    private static final double usdRonRate = 4.0915;// * 0.9958d;

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    @Autowired
    private RevolutService revolutService;

    public MetalPrice getMetalPrice(MetalType metalType) throws BusinessException {
        Optional<List<MetalPrice>> price = this.metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
        return price.map(metalPrices -> metalPrices.get(0)).orElse(null);
    }

    public MetalInfo calculatesUserProfit(Purchase purchase) {
        double revolutProfitPercentages = this.revolutService.getRevolutProfitFor(purchase.getMetalType());
        final MetalPrice metalPriceNow = this.getMetalPrice(purchase.getMetalType());
        double priceKgNow = metalPriceNow.getPrice();

        double revolutGoldPriceKg = priceKgNow * (revolutProfitPercentages + 1) * usdRonRate;
        double revolutGoldPriceOunce = revolutGoldPriceKg * Util.ounce;
        double costNowUser = revolutGoldPriceOunce * purchase.getAmount();
        double profitRevolut = costNowUser - purchase.getCost();
        return MetalInfo
                .builder()
                .metalSymbol(purchase.getMetalSymbol())
                .amountPurchased(purchase.getAmount())
                .costPurchased(purchase.getCost())
                .purchaseTime(purchase.getTime())
                .costNow(costNowUser)
                .profit(profitRevolut)
                .build();
    }


    public void save(MetalPrice price) {
        this.metalPriceRepository.save(price);
    }
}
