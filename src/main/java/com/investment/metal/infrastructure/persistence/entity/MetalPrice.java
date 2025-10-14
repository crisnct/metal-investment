package com.investment.metal.infrastructure.persistence.entity;


import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "metalprices")
@Data
public class MetalPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "metal_symbol")
    private String metalSymbol;

    private double price;

    private Timestamp time;

}
