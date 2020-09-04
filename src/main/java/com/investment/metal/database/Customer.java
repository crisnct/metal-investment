package com.investment.metal.database;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String email;


}