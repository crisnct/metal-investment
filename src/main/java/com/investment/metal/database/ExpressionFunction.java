package com.investment.metal.database;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "expressionfunction")
@Data
public class ExpressionFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "returned_type")
    private String returnedType;

}
