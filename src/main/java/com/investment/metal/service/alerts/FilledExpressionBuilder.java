package com.investment.metal.service.alerts;

public class FilledExpressionBuilder {

    private final String expression;

    private double profit;

    public FilledExpressionBuilder(String expression) {
        this.expression = expression;
    }

    public FilledExpressionBuilder setProfit(double profit) {
        this.profit = profit;
        return this;
    }

    public String build() {
        return this.expression.replaceAll("profit", String.valueOf(this.profit));
    }
}
