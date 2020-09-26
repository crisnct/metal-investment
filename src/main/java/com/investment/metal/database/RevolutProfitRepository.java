package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RevolutProfitRepository extends JpaRepository<RevolutProfit, Long> {
    @Query(value="SELECT * FROM RevolutProfit WHERE metal_symbol=?1 ORDER BY time DESC LIMIT 1", nativeQuery = true)
    RevolutProfit findByMetalSymbol(String metalSymbol);

}
