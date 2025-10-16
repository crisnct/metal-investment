package com.investment.metal.infrastructure.persistence.adapter;

import com.investment.metal.domain.model.Currency;
import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.repository.CurrencyRepository;
import com.investment.metal.infrastructure.mapper.CurrencyMapper;
import com.investment.metal.infrastructure.persistence.repository.CurrencyJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter implementing the domain {@link CurrencyRepository}.
 */
@Component
public class CurrencyRepositoryAdapter implements CurrencyRepository {

    private final CurrencyJpaRepository currencyJpaRepository;
    private final CurrencyMapper currencyMapper;

    public CurrencyRepositoryAdapter(CurrencyJpaRepository currencyJpaRepository,
                                     CurrencyMapper currencyMapper) {
        this.currencyJpaRepository = currencyJpaRepository;
        this.currencyMapper = currencyMapper;
    }

    @Override
    public Optional<Currency> findByType(CurrencyType type) {
        return Optional.ofNullable(currencyJpaRepository.findBySymbol(type.name()))
                .map(currencyMapper::toDomain);
    }

    @Override
    public Currency save(Currency currency) {
        var saved = currencyJpaRepository.save(currencyMapper.toEntity(currency));
        return currencyMapper.toDomain(saved);
    }
}
