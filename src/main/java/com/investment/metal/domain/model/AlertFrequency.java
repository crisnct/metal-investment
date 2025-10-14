package com.investment.metal.domain.model;

/**
 * Domain model for Alert Frequency following Domain-Driven Design principles.
 * Represents the frequency at which alerts should be checked.
 */
public enum AlertFrequency {
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY;

    public static AlertFrequency lookup(String value) {
        try {
            return AlertFrequency.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
