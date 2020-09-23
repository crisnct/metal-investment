package com.investment.metal.service.alerts;

import com.investment.metal.MessageKey;
import com.investment.metal.common.AlertFrequency;
import com.investment.metal.common.MetalType;
import com.investment.metal.database.Alert;
import com.investment.metal.database.AlertRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.MetalPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService extends AbstractService {

    private final ScriptEngine engine;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MetalPriceService metalPricesService;

    public AlertService() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    public void addAlert(Long userId, String expression, AlertFrequency frequency, MetalType metalType) throws BusinessException {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setMetalSymbol(metalType.getSymbol());
        alert.setExpression(expression);
        alert.setFrequency(frequency.name());
        alert.setLastTimeChecked(new Timestamp(System.currentTimeMillis()));
        this.alertRepository.save(alert);
    }

    public List<Alert> findAllByMetalSymbol(String metalSymbol) {
        return this.alertRepository.findByMetalSymbol(metalSymbol).orElse(new ArrayList<>());
    }

    public boolean evaluateExpression(String expression, double profit) throws ScriptException {
        final String filledExpression = new FilledExpressionBuilder(expression)
                .setProfit(profit)
                .build();
        return (Boolean) this.engine.eval(filledExpression);
    }

    public void saveAll(List<Alert> allAlerts) {
        this.alertRepository.saveAll(allAlerts);
    }

    public List<Alert> findAllByUserId(Long userId) {
        return this.alertRepository.findByUserId(userId).orElse(new ArrayList<>());
    }

    public void removeAlert(long alertId) throws BusinessException {
        Optional<Alert> alert = this.alertRepository.findById(alertId);
        if (alert.isPresent()) {
            this.alertRepository.delete(alert.get());
        } else
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid alert id")
                    .build();
    }

}
