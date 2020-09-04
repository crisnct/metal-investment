package com.investment.metal;

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

    public abstract String getSymbol();
}
