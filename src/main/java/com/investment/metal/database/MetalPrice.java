package com.investment.metal.database;


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

    private String metalSymbol;

    private double price;

    private Timestamp time;

}
