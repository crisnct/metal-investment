package com.investment.metal.service;

public enum CurrencyType {
    USD {
        @Override
        public String getFeelURL() {
            return "https://www.bnr.ro/RSS_200004_USD.aspx";
        }
    },
    RON {
        @Override
        public String getFeelURL() {
            return null;
        }
    },
    EUR {
        @Override
        public String getFeelURL() {
            return "https://www.bnr.ro/RSS_200003_EUR.aspx";
        }
    },
    GBP {
        @Override
        public String getFeelURL() {
            return "https://www.bnr.ro/RSS_200014_GBP.aspx";
        }
    };

    public abstract String getFeelURL();
}
