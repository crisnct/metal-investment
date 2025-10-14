package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUsernameAndPassword(String username, String password);

    Optional<Customer> findByUsername(String username);

    Optional<Customer> findByEmail(String email);
}
