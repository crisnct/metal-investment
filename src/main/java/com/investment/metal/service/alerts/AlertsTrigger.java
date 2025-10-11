package com.investment.metal.service.alerts;

import com.investment.metal.common.MetalType;
import com.investment.metal.database.Alert;
import com.investment.metal.database.Customer;
import com.investment.metal.database.MetalPrice;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.UserMetalInfoDto;
import com.investment.metal.service.*;
import com.investment.metal.service.exception.ExceptionService;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class AlertsTrigger {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsTrigger.class);

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

    public void triggerAlerts(MetalType metalType) {
        final Map<Long, UserProfit> usersProfit = this.calculateUsersProfit(metalType);

        final List<Alert> allAlerts = this.alertService.findAllByMetalSymbol(metalType.getSymbol());
        for (Alert alert : allAlerts) {
            if (this.isTimeToCheckAlert(alert)) {
                final UserProfit userProfit = usersProfit.get(alert.getUserId());
                this.triggerAlert(userProfit, alert);
                alert.setLastTimeChecked(new Timestamp(System.currentTimeMillis()));
            }
        }

        this.alertService.saveAll(allAlerts);
    }

    private void triggerAlert(UserProfit userProfit, Alert alert) {
        String expression = alert.getExpression();
        final double profit = userProfit.getProfit();
        try {
            ExpressionEvaluator eval = this.alertService.evaluateExpression(expression)
                    .setParameters((name, params) -> {
                        switch (name) {
                            case "profit":
                                return profit;
                            case "inc":
                                int days = Integer.parseInt(params[0].toString());
                                double eps = Double.parseDouble(params[1].toString());
                                return getIncremental(alert.getMetalType(), days, eps);
                            default:
                                return null;
                        }
                    });

            if (eval.evaluate()) {
                this.emailService.sendMailWithProfit(userProfit, alert);
            }
        } catch (ScriptException e) {
            LOGGER.error("Invalid expression: " + expression, e);
        }
    }

    private boolean getIncremental(MetalType metalType, int days, double eps) {
        Optional<List<MetalPrice>> pricesOp = this.metalPricesService.getMetalPriceAll(metalType);
        boolean isInc = false;
        if (pricesOp.isPresent()) {
            final Timestamp ts = new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days));
            List<MetalPrice> prices = pricesOp.get()
                    .stream()
                    .filter(m -> m.getTime().after(ts))
                    .sorted(Comparator.comparing(MetalPrice::getTime))
                    .collect(Collectors.toList());
            double prevPrice = -Double.MAX_VALUE;
            if (prices.size() > 1) {
                isInc = true;
                for (MetalPrice price : prices) {
                    double epsPrice = prevPrice * eps / 100.0d;
                    if (price.getPrice() < prevPrice - epsPrice) {
                        isInc = false;
                        break;
                    }
                    prevPrice = price.getPrice();
                }
            }
        }
        return isInc;
    }

    private Map<Long, UserProfit> calculateUsersProfit(MetalType metalType) {
        final List<Customer> allUsers = this.accountService.findAll();
        final Map<Long, UserProfit> usersProfit = new HashMap<>();
        for (Customer user : allUsers) {
            Purchase purchase = this.purchaseService.getPurchase(user.getId(), metalType.getSymbol());
            double totalProfit = 0;
            double totalCost = 0;
            double totalCostNow = 0;
            double totalAmount = 0;
            if (purchase != null) {
                final UserMetalInfoDto info = this.metalPricesService.calculatesUserProfit(purchase);
                totalProfit += info.getProfit();
                totalCost += purchase.getCost();
                totalAmount += purchase.getAmount();
                totalCostNow += info.getCostNow();
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
