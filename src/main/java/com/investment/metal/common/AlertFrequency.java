package com.investment.metal.common;

public enum AlertFrequency {
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY;

    public static AlertFrequency lookup(String value) {
        try {
            return AlertFrequency.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
