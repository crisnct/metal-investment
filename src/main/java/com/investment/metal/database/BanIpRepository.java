package com.investment.metal.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BanIpRepository extends JpaRepository<BanIp, Long> {

    Optional<BanIp> findByUserIdAndIp(long userId, String ip);

}
