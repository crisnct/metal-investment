package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.ExpressionFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpressionFunctionRepository extends JpaRepository<ExpressionFunction, Integer> {

}
