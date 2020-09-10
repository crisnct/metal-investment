package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.MetalType;
import com.investment.metal.database.Purchase;
import com.investment.metal.database.PurchaseRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PurchaseService extends AbstractService {

    @Autowired
    private PurchaseRepository purchaseRepo;

    public void purchase(long userId, double metalAmount, MetalType metalType, double cost) {
        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setAmount(metalAmount);
        purchase.setMetalSymbol(metalType.getSymbol());
        purchase.setTime(new Timestamp(System.currentTimeMillis()));
        purchase.setCost(cost);
        this.purchaseRepo.save(purchase);
    }

    public void sell(Long userId, double metalAmount, MetalType metalType) throws BusinessException {
        final List<Purchase> purchases = this.purchaseRepo.findByUserIdAndMetalSymbol(userId, metalType.getSymbol());
        final double totalAmount = purchases.stream().map(Purchase::getAmount).reduce(Double::sum).orElse(0.0d);
        this.exceptionService.check(metalAmount > totalAmount, MessageKey.SELL_MORE_THAN_YOU_HAVE, metalType.getSymbol(), totalAmount);

        double toSubstract = metalAmount;
        for (Purchase purchase : purchases) {
            double amountPurchase = purchase.getAmount();
            if (toSubstract >= amountPurchase) {
                this.purchaseRepo.delete(purchase);
                toSubstract -= amountPurchase;
                //
            } else if (toSubstract < amountPurchase) {
                double newAmount = amountPurchase - toSubstract;
                double newCost = purchase.getCost() * (newAmount) / amountPurchase;
                purchase.setAmount(newAmount);
                purchase.setCost(newCost);
                this.purchaseRepo.save(purchase);
                break;
            }
        }
    }

    public List<Purchase> getAllPurchase(long userId) {
        return this.purchaseRepo.findByUserId(userId);
    }

}
