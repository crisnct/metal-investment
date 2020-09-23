package com.investment.metal.service.alerts;

import com.investment.metal.common.MetalType;
import com.investment.metal.database.Alert;
import com.investment.metal.database.Customer;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.UserMetalInfo;
import com.investment.metal.service.*;
import com.investment.metal.service.exception.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AlertsTrigger {

    @Autowired
    protected ExceptionService exceptionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MetalPriceService metalPricesService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private EmailService emailService;

    public void trigerAlerts(MetalType metalType) {
        final Map<Long, UserProfit> usersProfit = this.calculateUsersProfit();

        final List<Alert> allAlerts = this.alertService.findAllByMetalSymbol(metalType.getSymbol());
        for (Alert alert : allAlerts) {
            if (this.isTimeToCheckAlert(alert)) {
                alert.setLastTimeChecked(new Timestamp(System.currentTimeMillis()));
                UserProfit userProfit = usersProfit.get(alert.getUserId());
                double profit = userProfit.getProfit();
                try {
                    if (this.alertService.evaluateExpression(alert.getExpression(), profit)) {
                        this.emailService.sendMailWithProfit(userProfit, alert);
                    }
                } catch (ScriptException e) {
                    System.err.println("Invalid expression: " + alert.getExpression());
                }
            }
        }

        this.alertService.saveAll(allAlerts);
    }

    private Map<Long, UserProfit> calculateUsersProfit() {
        final List<Customer> allUsers = this.accountService.findAll();
        final Map<Long, UserProfit> usersProfit = new HashMap<>();
        for (Customer user : allUsers) {
            List<Purchase> purchases = this.purchaseService.getAllPurchase(user.getId());
            double totalProfit = 0;
            double totalCost = 0;
            double totalCostNow = 0;
            double totalAmount = 0;
            if (!purchases.isEmpty()) {
                for (Purchase purchase : purchases) {
                    final UserMetalInfo info = this.metalPricesService.calculatesUserProfit(purchase);
                    totalProfit += info.getProfit();
                    totalCost += purchase.getCost();
                    totalAmount += purchase.getAmount();
                    totalCostNow += info.getCostNow();
                }
            }
            final UserProfit info = UserProfit.builder()
                    .profit(totalProfit)
                    .metalAmount(totalAmount)
                    .originalCost(totalCost)
                    .currentCost(totalCostNow)
                    .user(user)
                    .build();
            usersProfit.put(user.getId(), info);
        }
        return usersProfit;
    }

    private boolean isTimeToCheckAlert(Alert alert) {
        long diff = System.currentTimeMillis() - alert.getLastTimeChecked().getTime();
        switch (alert.getFrequency()) {
            case HOURLY:
                return diff > TimeUnit.HOURS.toMillis(1);
            case DAILY:
                return diff > TimeUnit.DAYS.toMillis(1);
            case WEEKLY:
                return diff > TimeUnit.DAYS.toMillis(7);
            case MONTHLY:
                return diff > TimeUnit.DAYS.toMillis(30);
        }
        return false;
    }

}
