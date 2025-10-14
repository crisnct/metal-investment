package com.investment.metal.infrastructure.persistence.entity;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "expressionfunctionparameters")
@Data
public class ExpressionParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "expression_function_id")
    private Integer expressionFunctionId;

    private String name;

    private double min;

    private double max;

}
