package com.investment.metal.database;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "expressionfunction")
@Data
public class ExpressionFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String returnedType;

}
