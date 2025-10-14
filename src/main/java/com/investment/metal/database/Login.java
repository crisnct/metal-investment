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
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    private Timestamp time;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "login_token")
    private String loginToken;

    @Column(name = "validation_code")
    private int validationCode;

    private Integer validated;

    @Column(name = "logged_in")
    private Integer loggedIn;

    @Column(name = "failed_attempts")
    private int failedAttempts;

    @Column(name = "token_expire_time")
    private Timestamp tokenExpireTime;

}
