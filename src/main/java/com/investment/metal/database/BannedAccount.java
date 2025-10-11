package com.investment.metal.database;

import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "bannedaccounts")
@Data
public class BannedAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Timestamp bannedUntil;

    private String reason;

}
