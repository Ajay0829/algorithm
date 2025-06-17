package com.market.streamline.repository;

import com.market.streamline.entity.LiquiditySweep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiquiditySweepRepository extends JpaRepository<LiquiditySweep, Long> {
    // Basic CRUD methods are inherited from JpaRepository
}

