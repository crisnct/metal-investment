package com.investment.metal.service.alerts;

import com.google.common.collect.Maps;
import com.investment.metal.MessageKey;
import com.investment.metal.common.AlertFrequency;
import com.investment.metal.common.MetalType;
import com.investment.metal.database.*;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.MessageService;
import com.investment.metal.application.service.MetalPriceService;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
//import jakarta.script.ScriptEngine;
//import jakarta.script.ScriptEngineManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AlertService extends AbstractService {

    private final ScriptEngine engine;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MetalPriceService metalPricesService;

    @Autowired
    private ExpressionFunctionRepository functionRepository;

    @Autowired
    private ExpressionParameterRepository parameterRepository;

    @Autowired
    protected MessageService messageService;

    private final Map<String, FunctionInfo> expressionFunctions = Maps.newLinkedHashMap();

    public AlertService() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    @PostConstruct
    public void init() {
        for (ExpressionFunction func : functionRepository.findAll()) {
            String functionName = func.getName();
            FunctionInfo info = new FunctionInfo(functionName);
            info.setDescription(messageService.getMessage("FUNCTION_" + functionName));
            info.setReturnedType(func.getReturnedType());

            Optional<List<ExpressionParameter>> paramsOp = parameterRepository.findByExpressionFunctionId(func.getId());
            if (paramsOp.isPresent()) {
                for (ExpressionParameter param : paramsOp.get()) {
                    String paramName = param.getName();
                    info.addParam(FunctionParam.builder()
                            .name(paramName)
                            .description(messageService.getMessage("FUNCTION_PARAM_" + functionName + "_" + paramName))
                            .min(param.getMin())
                            .max(param.getMax())
                            .build());
                }
            }
            expressionFunctions.put(functionName, info);
        }
    }

    public Map<String, FunctionInfo> getExpressionFunctions() {
        return expressionFunctions;
    }

    public void addAlert(Integer userId, String expression, AlertFrequency frequency, MetalType metalType) throws BusinessException {
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

    public ExpressionEvaluator evaluateExpression(String expression) {
        return new ExpressionEvaluator(expression, this.engine, getExpressionFunctions());
    }

    public void saveAll(List<Alert> allAlerts) {
        this.alertRepository.saveAll(allAlerts);
    }

    public List<Alert> findAllByUserId(Integer userId) {
        return this.alertRepository.findByUserId(userId).orElse(new ArrayList<>());
    }

    public void removeAlert(Integer alertId) throws BusinessException {
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
