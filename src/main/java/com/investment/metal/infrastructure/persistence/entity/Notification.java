package com.investment.metal.infrastructure.persistence.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private int frequency;

    @Column(name = "last_time_notified")
    private Timestamp lastTimeNotified;

}
