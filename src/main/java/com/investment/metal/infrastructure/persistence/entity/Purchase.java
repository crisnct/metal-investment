package com.investment.metal.infrastructure.persistence.entity;

import com.investment.metal.domain.model.MetalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.Data;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "metal_symbol")
    private String metalSymbol;

    private Double amount;

    private Double cost;

    private Timestamp time;

    public MetalType getMetalType() {
        return MetalType.lookup(this.metalSymbol);
    }

}
