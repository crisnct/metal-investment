package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpressionFunctionRepository extends JpaRepository<ExpressionFunction, Integer> {

}
