package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevolutProfitRepository extends JpaRepository<RevolutProfit, Long> {

    RevolutProfit findByMetalSymbol(String metalSymbol);

}
