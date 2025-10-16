package com.investment.metal.application.service;

import com.investment.metal.application.dto.UserMetalInfoDto;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.persistence.entity.Alert;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.EmailService;
import com.investment.metal.infrastructure.service.UserProfit;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.script.ScriptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AlertsTrigger {

    @Autowired
    protected ExceptionService exceptionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private com.investment.metal.application.service.MetalPriceService metalPricesService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private EmailService emailService;

    public void triggerAlerts(MetalType metalType) {
        final Map<Integer, UserProfit> usersProfit = this.calculateUsersProfit(metalType);

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
            log.error("Invalid expression: " + expression, e);
        }
    }

    private boolean getIncremental(MetalType metalType, int days, double eps) {
        Optional<List<com.investment.metal.domain.model.MetalPrice>> pricesOp = this.metalPricesService.getMetalPriceAll(metalType);
        boolean isInc = false;
        if (pricesOp.isPresent()) {
            final LocalDateTime threshold = LocalDateTime.now().minusDays(days);
            List<com.investment.metal.domain.model.MetalPrice> prices = pricesOp.get()
                    .stream()
                    .filter(m -> m.getTimestamp() != null && m.getTimestamp().isAfter(threshold))
                    .sorted(Comparator.comparing(com.investment.metal.domain.model.MetalPrice::getTimestamp))
                    .collect(Collectors.toList());
            double prevPrice = -Double.MAX_VALUE;
            if (prices.size() > 1) {
                isInc = true;
                for (com.investment.metal.domain.model.MetalPrice price : prices) {
                    double currentPrice = price.getPrice().doubleValue();
                    double epsPrice = prevPrice * eps / 100.0d;
                    if (currentPrice < prevPrice - epsPrice) {
                        isInc = false;
                        break;
                    }
                    prevPrice = currentPrice;
                }
            }
        }
        return isInc;
    }

    /**
     * Calculate profit information for all users for a specific metal type.
     * This method iterates through all users and calculates their profit/loss
     * based on their metal purchases and current market prices.
     * 
     * @param metalType the type of metal to calculate profits for
     * @return map of user ID to their profit information
     */
    private Map<Integer, UserProfit> calculateUsersProfit(MetalType metalType) {
        final List<Customer> allUsers = this.accountService.findAll();
        final Map<Integer, UserProfit> usersProfit = new HashMap<>();
        for (Customer user : allUsers) {
            MetalPurchase metalPurchase = this.purchaseService.getPurchase(user.getId(), metalType.getSymbol());
            double totalProfit = 0;
            double totalCost = 0;
            double totalCostNow = 0;
            double totalAmount = 0;
            if (metalPurchase != null) {
                final UserMetalInfoDto info = this.metalPricesService.calculatesUserProfit(metalPurchase);
                totalProfit += info.getProfit();
                totalCost += metalPurchase.getCost().doubleValue();
                totalAmount += metalPurchase.getAmount().doubleValue();
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

    /**
     * Check if it's time to evaluate an alert based on its frequency.
     * Compares the time since last check against the alert's frequency setting.
     * 
     * @param alert the alert to check
     * @return true if enough time has passed to check the alert again
     */
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
