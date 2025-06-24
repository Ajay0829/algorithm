package com.market.streamline.repository;

import com.market.streamline.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    // Basic CRUD methods are inherited from JpaRepository
    List<Zone> findByStockSymbol(String stockSymbol);
}

