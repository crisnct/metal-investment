package com.investment.metal.infrastructure.persistence.repository;

import com.investment.metal.infrastructure.persistence.entity.MetalPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetalPriceRepository extends JpaRepository<MetalPrice, Integer> {

    @Query("select t from MetalPrice t where t.metalSymbol=?1 order by t.id desc")
    Optional<List<MetalPrice>> findByMetalSymbol(String symbol);

}
