package com.investment.metal.infrastructure.service;

import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.infrastructure.persistence.entity.Currency;
import com.investment.metal.infrastructure.persistence.repository.CurrencyRepository;
import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Infrastructure service for managing currency exchange rates.
 * Handles currency data persistence and retrieval.
 */
@Service
public class CurrencyService extends AbstractService {

    @Autowired
    private CurrencyRepository currencyRepository;

    /**
     * Find currency by symbol.
     * 
     * @param type the currency type to find
     * @return the currency entity or null if not found
     */
    public Currency findBySymbol(CurrencyType type) {
        return this.currencyRepository.findBySymbol(type.name());
    }

    /**
     * Save currency exchange rate.
     * 
     * @param currencyType the type of currency
     * @param value the exchange rate value
     */
    public void save(CurrencyType currencyType, double value) {
        Currency curr = this.findBySymbol(currencyType);
        if (curr == null) {
            curr = new Currency();
        }
        curr.setRon(value);
        curr.setSymbol(currencyType.name());
        curr.setTime(new Timestamp(System.currentTimeMillis()));
        this.currencyRepository.save(curr);
    }
}
