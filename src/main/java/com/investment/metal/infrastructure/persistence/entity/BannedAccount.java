package com.investment.metal.infrastructure.persistence.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "bannedaccounts")
@Data
public class BannedAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "banned_until")
    private Timestamp bannedUntil;

    private String reason;

}
