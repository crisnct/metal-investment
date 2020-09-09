package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<List<Alert>> findByUserId(long userId);

    Optional<List<Alert>> findByUserIdAndMetalSymbol(long userId, String metalSymbol);

    Optional<List<Alert>> findByMetalSymbol(String metalSymbol);

}
