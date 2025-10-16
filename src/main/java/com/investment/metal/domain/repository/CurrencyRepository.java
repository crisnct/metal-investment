package com.investment.metal.domain.repository;

import com.investment.metal.domain.model.Currency;
import com.investment.metal.domain.model.CurrencyType;
import java.util.Optional;

/**
 * Domain repository abstraction for {@link Currency}.
 */
public interface CurrencyRepository {

    Optional<Currency> findByType(CurrencyType type);

    Currency save(Currency currency);
}
