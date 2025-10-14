package com.investment.metal.infrastructure.persistence.entity;

import com.investment.metal.domain.model.AlertFrequency;
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
