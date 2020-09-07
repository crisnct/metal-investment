package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {

    Optional<Login> findByToken(String token);

    Optional<Login> findByUserIdAndValidationCode(long userId, int code);

    Optional<Login> findByUserId(Long id);
}