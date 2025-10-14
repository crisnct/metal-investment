package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {

    Optional<List<Alert>> findByUserId(Integer userId);

    Optional<List<Alert>> findByUserIdAndMetalSymbol(Integer userId, String metalSymbol);

    Optional<List<Alert>> findByMetalSymbol(String metalSymbol);

}
