package com.investment.metal.database;

import com.investment.metal.common.AlertFrequency;
import com.investment.metal.common.MetalType;
import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "alerts")
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String metalSymbol;

    private String expression;

    private String frequency;

    private Timestamp lastTimeChecked;

    public AlertFrequency getFrequency() {
        return AlertFrequency.valueOf(this.frequency);
    }

    public MetalType getMetalType() {
        return MetalType.lookup(this.metalSymbol);
    }
}
