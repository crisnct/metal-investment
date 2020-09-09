package com.investment.metal.service.impl;

import com.investment.metal.MetalType;
import com.investment.metal.Util;
import com.investment.metal.database.Currency;
import com.investment.metal.database.MetalPrice;
import com.investment.metal.database.MetalPriceRepository;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.MetalInfo;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.CurrencyType;
import com.investment.metal.service.ExternalMetalPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MetalPricesService extends AbstractService {

    private static final int THRESHOLD_TOO_OLD_ENTITIES = 24 * 3600 * 1000;

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    @Autowired
    private RevolutService revolutService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExternalMetalPriceService externalPriceService;

    public MetalPrice getMetalPrice(MetalType metalType) throws BusinessException {
        Optional<List<MetalPrice>> price = this.metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
        return price.map(metalPrices -> metalPrices.get(0)).orElse(null);
    }

    public double fetchMetalPrice(MetalType metalType) {
        return this.externalPriceService.fetchPrice(metalType);
    }

    public MetalInfo calculatesUserProfit(Purchase purchase) {
        final Currency currency = this.currencyService.findBySymbol(CurrencyType.USD);
        final double usdRonRate = currency.getRon();

        double revolutProfitPercentages = this.revolutService.getRevolutProfitFor(purchase.getMetalType());
        final double metalPriceNowKg = this.externalPriceService.fetchPrice(purchase.getMetalType());

        double revolutGoldPriceKg = metalPriceNowKg * (revolutProfitPercentages + 1) * usdRonRate;
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

    public void save(MetalType metalType, double price) {
        final Timestamp timeThreshold = new Timestamp(System.currentTimeMillis() - THRESHOLD_TOO_OLD_ENTITIES);
        final List<MetalPrice> tooOldEntities = this.metalPriceRepository
                .findByMetalSymbol(metalType.getSymbol()).orElse(new ArrayList<>())
                .stream()
                .filter(p -> p.getTime().before(timeThreshold))
                .collect(Collectors.toList());
        if (!tooOldEntities.isEmpty()) {
            this.metalPriceRepository.deleteAll(tooOldEntities);
        }

        final MetalPrice entity = new MetalPrice();
        entity.setMetalSymbol(metalType.getSymbol());
        entity.setPrice(price);
        entity.setTime(new Timestamp(System.currentTimeMillis()));
        this.metalPriceRepository.save(entity);
    }
}
