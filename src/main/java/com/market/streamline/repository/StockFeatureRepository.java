package com.market.streamline.repository;

import com.market.streamline.entity.StockFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockFeatureRepository extends JpaRepository<StockFeature, Long> {
    // Basic CRUD methods are inherited from JpaRepository
}

