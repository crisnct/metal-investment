package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.ExpressionParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpressionParameterRepository extends JpaRepository<ExpressionParameter, Integer> {

    Optional<List<ExpressionParameter>> findByExpressionFunctionId(Integer id);

}
