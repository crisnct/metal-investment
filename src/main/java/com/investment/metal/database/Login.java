package com.investment.metal.database;


import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "login")
@Data
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private long time;

    private String token;

    private int validationCode;

}