package com.investment.metal.database;

import com.investment.metal.exceptions.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    Optional<List<Purchase>> findByUserId(Integer userId);

    Optional<Purchase> findByUserIdAndMetalSymbol(Integer userId, String metalSymbol) throws BusinessException;

}
