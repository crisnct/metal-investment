package com.investment.metal.infrastructure.persistence.entity;

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

    @Column(name = "metal_symbol")
    private String metalSymbol;

    private Double profit;

    private Timestamp time;

    @Column(name = "revolut_price_oz")
    private Double revolutPriceOz;

    @Column(name = "metal_price_oz")
    private Double metalPriceOz;

    @Column(name = "currency_to_ron_rate")
    private Double currencyToRonRate;


}
