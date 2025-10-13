package com.investment.metal.service;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import com.investment.metal.database.Currency;
import com.investment.metal.database.MetalPrice;
import com.investment.metal.database.MetalPriceRepository;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.UserMetalInfoDto;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.price.ExternalMetalPriceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MetalPriceService extends AbstractService {

    public static final long THRESHOLD_TOO_OLD_ENTITIES = TimeUnit.DAYS.toMillis(14);

    private static final Logger LOGGER = LoggerFactory.getLogger(MetalPriceService.class);

    @Autowired
    private MetalPriceRepository metalPriceRepository;

    @Autowired
    private RevolutService revolutService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExternalMetalPriceReader externalPriceService;

    public MetalPrice getMetalPrice(MetalType metalType) throws BusinessException {
        Optional<List<MetalPrice>> price = this.metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
        return price.map(metalPrices -> metalPrices.get(0)).orElse(null);
    }

    public Optional<List<MetalPrice>> getMetalPriceAll(MetalType metalType) throws BusinessException {
        return this.metalPriceRepository.findByMetalSymbol(metalType.getSymbol());
    }

    public double fetchMetalPrice(MetalType metalType) {
        return this.externalPriceService.fetchPrice(metalType);
    }

    public UserMetalInfoDto calculatesUserProfit(Purchase purchase) {
        CurrencyType currencyType = this.externalPriceService.getCurrencyType();
        final Currency currency = this.currencyService.findBySymbol(currencyType);
        final double currencyToRonRate = currency.getRon();

        double revolutProfitPercentages = this.revolutService.getRevolutProfitFor(purchase.getMetalType());
        final double metalPriceNowKg = this.externalPriceService.fetchPrice(purchase.getMetalType());

        double revolutGoldPriceKg = metalPriceNowKg * (revolutProfitPercentages + 1) * currencyToRonRate;
        double revolutGoldPriceOunce = revolutGoldPriceKg * Util.OUNCE;
        double costNowUser = revolutGoldPriceOunce * purchase.getAmount();
        double profitRevolut = costNowUser - purchase.getCost();
        return UserMetalInfoDto
                .builder()
                .metalSymbol(purchase.getMetalSymbol())
                .amountPurchased(purchase.getAmount())
                .costPurchased(purchase.getCost())
                .costNow(costNowUser)
                .profit(profitRevolut)
                .build();
    }

    /**
     * Calculate the revolut price per ounce for which the user would have the given profit
     */
    public double calculatesRevolutPrice(Purchase purchase, double profit) {
        return (profit + purchase.getCost()) / purchase.getAmount();
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
