package com.investment.metal.infrastructure.persistence.entity;

import com.investment.metal.domain.model.AlertFrequency;
import com.investment.metal.domain.model.MetalType;
import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "alerts")
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "metal_symbol")
    private String metalSymbol;

    private String expression;

    private String frequency;

    @Column(name = "last_time_checked")
    private Timestamp lastTimeChecked;

    public AlertFrequency getFrequency() {
        return AlertFrequency.valueOf(this.frequency);
    }

    public MetalType getMetalType() {
        return MetalType.lookup(this.metalSymbol);
    }
    
    public Timestamp getLastTimeChecked() {
        return this.lastTimeChecked;
    }
    
    public void setLastTimeChecked(Timestamp lastTimeChecked) {
        this.lastTimeChecked = lastTimeChecked;
    }
}
