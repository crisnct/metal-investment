package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.RevolutProfit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RevolutProfitRepository extends JpaRepository<RevolutProfit, Integer> {
    @Query(value="SELECT * FROM revolutprofit WHERE metal_symbol=?1 ORDER BY time DESC LIMIT 1", nativeQuery = true)
    RevolutProfit findByMetalSymbol(String metalSymbol);

}
