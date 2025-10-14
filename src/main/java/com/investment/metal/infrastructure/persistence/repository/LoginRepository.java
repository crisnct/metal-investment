package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Login, Integer> {

    Optional<Login> findByLoginToken(String token);

    Optional<Login> findByUserId(Integer id);
}
