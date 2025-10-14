package com.investment.metal.application.service;

import com.google.common.collect.Maps;
import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.model.AlertFrequency;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.persistence.entity.Alert;
import com.investment.metal.infrastructure.persistence.entity.ExpressionFunction;
import com.investment.metal.infrastructure.persistence.entity.ExpressionParameter;
import com.investment.metal.infrastructure.persistence.repository.AlertRepository;
import com.investment.metal.infrastructure.persistence.repository.ExpressionFunctionRepository;
import com.investment.metal.infrastructure.persistence.repository.ExpressionParameterRepository;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.service.MessageService;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Application service for managing price alerts and mathematical expressions.
 * Handles alert creation, evaluation, and management for metal price monitoring.
 * Follows Clean Architecture principles by orchestrating domain and infrastructure concerns.
 */
@Service
public class AlertService {

    /**
     * Exception service for handling business exceptions
     */
    @Autowired
    private ExceptionService exceptionService;

    /**
     * JavaScript engine for evaluating mathematical expressions in alerts
     */
    private final ScriptEngine engine;

    /**
     * Repository for managing alert data persistence
     */
    @Autowired
    private AlertRepository alertRepository;

    /**
     * Service for retrieving current metal prices
     */
    @Autowired
    private MetalPriceService metalPricesService;

    /**
     * Repository for managing expression functions
     */
    @Autowired
    private ExpressionFunctionRepository functionRepository;

    /**
     * Repository for managing expression parameters
     */
    @Autowired
    private ExpressionParameterRepository parameterRepository;

    /**
     * Service for internationalized message handling
     */
    @Autowired
    protected MessageService messageService;

    /**
     * Cache of available expression functions for alert evaluation
     */
    @Getter
    private final Map<String, FunctionInfo> expressionFunctions = Maps.newLinkedHashMap();

    /**
     * Initialize the alert service with JavaScript engine for expression evaluation.
     * Sets up the script engine for mathematical expression processing.
     */
    public AlertService() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    /**
     * Initialize expression functions cache after dependency injection.
     * Loads all available mathematical functions and their parameters for alert evaluation.
     * This method is called automatically by Spring after bean creation.
     */
    @PostConstruct
    public void init() {
        for (ExpressionFunction func : functionRepository.findAll()) {
            String functionName = func.getName();
            FunctionInfo info = new FunctionInfo(functionName);
            info.setDescription(messageService.getMessage("FUNCTION_" + functionName));
            info.setReturnedType(func.getReturnedType());

            // Load function parameters with their constraints
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

    /**
     * Create a new price alert for a user.
     * 
     * @param userId the ID of the user creating the alert
     * @param expression the mathematical expression to evaluate
     * @param frequency how often to check the alert
     * @param metalType the type of metal to monitor
     * @throws BusinessException if alert creation fails
     */
    public void addAlert(Integer userId, String expression, AlertFrequency frequency, MetalType metalType) throws BusinessException {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setMetalSymbol(metalType.getSymbol());
        alert.setExpression(expression);
        alert.setFrequency(frequency.name());
        alert.setLastTimeChecked(new Timestamp(System.currentTimeMillis()));
        this.alertRepository.save(alert);
    }

    /**
     * Find all alerts for a specific metal symbol.
     * 
     * @param metalSymbol the symbol of the metal to find alerts for
     * @return list of alerts for the specified metal, or empty list if none found
     */
    public List<Alert> findAllByMetalSymbol(String metalSymbol) {
        return this.alertRepository.findByMetalSymbol(metalSymbol).orElse(new ArrayList<>());
    }

    /**
     * Create an expression evaluator for the given mathematical expression.
     * 
     * @param expression the mathematical expression to evaluate
     * @return ExpressionEvaluator instance for the expression
     */
    public ExpressionEvaluator evaluateExpression(String expression) {
        return new ExpressionEvaluator(expression, this.engine, getExpressionFunctions());
    }

    /**
     * Save multiple alerts in batch.
     * 
     * @param allAlerts list of alerts to save
     */
    public void saveAll(List<Alert> allAlerts) {
        this.alertRepository.saveAll(allAlerts);
    }

    /**
     * Find all alerts for a specific user.
     * 
     * @param userId the ID of the user
     * @return list of alerts for the user, or empty list if none found
     */
    public List<Alert> findAllByUserId(Integer userId) {
        return this.alertRepository.findByUserId(userId).orElse(new ArrayList<>());
    }

    /**
     * Remove an alert by its ID.
     * 
     * @param alertId the ID of the alert to remove
     * @throws BusinessException if the alert ID is invalid or alert not found
     */
    public void removeAlert(Integer alertId) throws BusinessException {
        Optional<Alert> alert = this.alertRepository.findById(alertId);
        if (alert.isPresent()) {
            this.alertRepository.delete(alert.get());
        } else {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid alert id")
                    .build();
        }
    }

}
