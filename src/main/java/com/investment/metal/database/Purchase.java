package com.investment.metal.database;

import com.investment.metal.common.MetalType;
import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String metalSymbol;

    private Double amount;

    private Double cost;

    private Timestamp time;

    public MetalType getMetalType() {
        return MetalType.lookup(this.metalSymbol);
    }

}
