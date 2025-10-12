package com.investment.metal.database;

import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "revolutprofit")
@Data
public class RevolutProfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String metalSymbol;

    private Double profit;

    private Timestamp time;

    private Double revolutPriceOz;

    private Double metalPriceOz;

    private Double currencyToRonRate;


}
