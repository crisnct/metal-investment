package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpressionParameterRepository extends JpaRepository<ExpressionParameter, Integer> {

    Optional<List<ExpressionParameter>> findByExpressionFunctionId(Integer id);

}
