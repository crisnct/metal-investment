package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.BannedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannedRepository extends JpaRepository<BannedAccount, Integer> {

    Optional<BannedAccount> findByUserId(Integer userId);

}
