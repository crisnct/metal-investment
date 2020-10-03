package com.investment.metal.service.alerts;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluator.class);

    private static final Pattern VARIABLES_REGEXP = Pattern.compile("([a-z])\\w+");

    private final String expression;

    private final ScriptEngine engine;

    private ExpressionFeeder expressionFeeder;

    private final Map<String, FunctionInfo> functions;

    public ExpressionEvaluator(String expression, ScriptEngine engine, Map<String, FunctionInfo> functions) {
        this.expression = expression;
        this.engine = engine;
        this.functions = functions;
    }

    public ExpressionEvaluator setParameters(ExpressionFeeder expressionFeeder) {
        this.expressionFeeder = expressionFeeder;
        return this;
    }

    public String isValid() {
        String errorMessage = null;
        try {
            Map<String, Object[]> vars = this.getVariables();
            if (vars.isEmpty()) {
                errorMessage = "Missing variables";
            } else {
                for (Map.Entry<String, Object[]> entry : this.getVariables().entrySet()) {
                    errorMessage = isValid(entry.getKey(), entry.getValue());
                    if (errorMessage != null) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            errorMessage = "Invalid expression: " + e.getMessage();
        }
        return errorMessage;
    }

    private String isValid(String var, Object[] params) {
        String errorMessage = null;
        final FunctionInfo funcInfo = this.functions.get(var);
        if (funcInfo == null) {
            errorMessage = "Undefined function: " + var;
        } else if (params != null) {
            int i = 0;
            if (funcInfo.getParameters().size() != params.length) {
                errorMessage = "Missing parameters for function: " + var;
            } else {
                for (FunctionParam param : funcInfo.getParameters()) {
                    double val = Double.parseDouble(params[i++].toString());
                    errorMessage = checkValue(val, param.getMin(), param.getMax());
                    if (errorMessage != null) {
                        break;
                    }
                }
            }
        }
        return errorMessage;
    }

    private String checkValue(double val, double min, double max) {
        if (val <= min || val >= max) {
            return String.format("The parameter %.4f is outside of interval[%.4f, %.4f]", val, min, max);
        } else {
            return null;
        }
    }

    public boolean evaluate() throws ScriptException {
        String filledExpression = this.expression;
        for (Map.Entry<String, Object[]> entry : this.getVariables().entrySet()) {
            String var = entry.getKey();
            Object[] params = entry.getValue();
            Object value = expressionFeeder.replace(var, params);
            if (params == null) {
                filledExpression = filledExpression.replace(var, value.toString());
            } else {
                int start = filledExpression.indexOf(var);
                int endPos = start + var.length();
                int endBracket = filledExpression.indexOf(')', endPos);
                String varWithParams = filledExpression.substring(start, endBracket + 1);
                filledExpression = filledExpression.replace(varWithParams, value.toString());
            }
        }
        return (Boolean) this.engine.eval(filledExpression);
    }

    private Map<String, Object[]> getVariables() {
        final Map<String, Object[]> vars = Maps.newLinkedHashMap();
        Matcher matcher = VARIABLES_REGEXP.matcher(this.expression);
        String exp = this.expression;
        while (matcher.find()) {
            String var = matcher.group();
            int endPos = exp.indexOf(var) + var.length();
            String[] params = null;
            if (exp.charAt(endPos) == '(') {
                int endBracket = exp.indexOf(')', endPos);
                params = exp.substring(endPos + 1, endBracket).split(",");
                exp = exp.substring(endBracket);
            } else {
                exp = exp.substring(endPos);
            }
            vars.put(var, params);
        }
        return vars;
    }

}
