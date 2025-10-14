package com.investment.metal.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * JPA entity representing a Customer (User) in the database.
 * Maps to the 'users' table in the database.
 * This is an infrastructure concern and should not contain business logic.
 */
@Entity
@Table(name = "users")
@Data
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    private String email;
}
