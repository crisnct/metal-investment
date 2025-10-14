package com.investment.metal.database;

import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "banip")
@Data
public class BanIp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private String ip;

    @Column(name = "blocked_until")
    private Timestamp blockedUntil;

    private String reason;

}
