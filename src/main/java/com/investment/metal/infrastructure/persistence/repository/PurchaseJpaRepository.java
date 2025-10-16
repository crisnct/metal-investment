package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseJpaRepository extends JpaRepository<Purchase, Integer> {

    Optional<List<Purchase>> findByUserId(Integer userId);

    Optional<Purchase> findByUserIdAndMetalSymbol(Integer userId, String metalSymbol) throws BusinessException;

    void deleteByUserIdAndMetalSymbol(Integer userId, String metalSymbol);

}
