package com.investment.metal.database;


import lombok.Data;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "login")
@Data
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Timestamp time;

    private String resetPasswordToken;

    private String loginToken;

    private int validationCode;

    private Boolean validated;

    private Boolean loggedIn;

    private int failedAttempts;

    private Timestamp tokenExpireTime;

}
