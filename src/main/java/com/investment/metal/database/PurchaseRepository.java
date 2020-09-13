package com.investment.metal.database;

import com.investment.metal.exceptions.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<List<Purchase>> findByUserId(long userId);

    Optional<Purchase> findByUserIdAndMetalSymbol(long userId, String metalSymbol) throws BusinessException;

}
