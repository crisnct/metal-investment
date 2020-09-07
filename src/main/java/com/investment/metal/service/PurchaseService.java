package com.investment.metal.service;

import com.investment.metal.MetalType;
import com.investment.metal.database.Purchase;
import com.investment.metal.database.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PurchaseService {

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

    public List<Purchase> getAllPurchase(long userId) {
        return this.purchaseRepo.findByUserId(userId);
    }

}
