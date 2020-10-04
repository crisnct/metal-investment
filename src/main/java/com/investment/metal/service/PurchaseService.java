package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.MetalType;
import com.investment.metal.database.Purchase;
import com.investment.metal.database.PurchaseRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseService extends AbstractService {

    @Autowired
    private PurchaseRepository purchaseRepo;

    public void purchase(long userId, double metalAmount, MetalType metalType, double cost) {
        Optional<Purchase> purchaseOp = this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalType.getSymbol());
        final Purchase purchase;
        if (purchaseOp.isPresent()) {
            purchase = purchaseOp.get();
            purchase.setAmount(metalAmount + purchase.getAmount());
            purchase.setCost(cost + purchase.getCost());
        } else {
            purchase = new Purchase();
            purchase.setUserId(userId);
            purchase.setMetalSymbol(metalType.getSymbol());
            purchase.setAmount(metalAmount);
            purchase.setCost(cost);
        }
        purchase.setTime(new Timestamp(System.currentTimeMillis()));
        this.purchaseRepo.save(purchase);
    }

    public void sell(Long userId, double metalAmount, MetalType metalType, double price) throws BusinessException {
        @SuppressWarnings("OptionalGetWithoutIsPresent") final Purchase purchase = this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalType.getSymbol()).get();
        this.exceptionService.check(metalAmount > purchase.getAmount(), MessageKey.SELL_MORE_THAN_YOU_HAVE, metalType.getSymbol(), purchase.getAmount());

        double amount = purchase.getAmount();
        double newAmount = amount - metalAmount;

        double newCost = purchase.getCost() - price;
        if (newCost < 0) {
            newCost = 0;
        }
        purchase.setAmount(newAmount);
        purchase.setCost(newCost);
        this.purchaseRepo.save(purchase);
    }

    public List<Purchase> getAllPurchase(long userId) {
        return this.purchaseRepo.findByUserId(userId).orElse(new ArrayList<>());
    }

    public Purchase getPurchase(long userId, String metalSymbol) {
        return this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalSymbol).orElse(null);
    }

}
