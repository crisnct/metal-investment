package com.investment.metal.database;

import com.investment.metal.exceptions.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByUserId(long userId);

    List<Purchase> findByUserIdAndMetalSymbol(long userId, String metalSymbol) throws BusinessException;

}
