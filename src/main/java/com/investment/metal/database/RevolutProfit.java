package com.investment.metal.database;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "revolutprofit")
@Data
public class RevolutProfit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metalSymbol;

    private Double profit;

    private Timestamp time;

    private Double revolutPriceOz;

    private Double currencyToRonRate;

    private Double metalPriceOz;

}
