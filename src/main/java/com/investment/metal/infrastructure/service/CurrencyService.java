package com.investment.metal.infrastructure.service;

import com.investment.metal.common.CurrencyType;
import com.investment.metal.infrastructure.persistence.entity.Currency;
import com.investment.metal.infrastructure.persistence.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class CurrencyService extends AbstractService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public Currency findBySymbol(CurrencyType type) {
        return this.currencyRepository.findBySymbol(type.name());
    }

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
