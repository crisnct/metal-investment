package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.BanIp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanIpRepository extends JpaRepository<BanIp, Integer> {

    Optional<BanIp> findByUserIdAndIp(Integer userId, String ip);

}
