package com.investment.metal.domain.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Domain model for Metal Type following Domain-Driven Design principles.
 * Represents the different types of precious metals in the domain.
 */
public enum MetalType {
    GOLD {
        @Override
        public String getSymbol() {
            return "AUX";
        }
    },
    SILVER {
        @Override
        public String getSymbol() {
            return "AGX";
        }
    },
    PLATINUM {
        @Override
        public String getSymbol() {
            return "PTX";
        }
    };

    public static MetalType lookup(String metalSymbol) {
        for (MetalType val : values()) {
            if (StringUtils.equalsAnyIgnoreCase(val.getSymbol(), metalSymbol)) {
                return val;
            }
        }
        return null;
    }

    public abstract String getSymbol();
}
