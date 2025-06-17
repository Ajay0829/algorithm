package com.market.streamline.repository;

import com.market.streamline.entity.SwingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwingPointRepository extends JpaRepository<SwingPoint, Long> {
    // Basic CRUD methods are inherited from JpaRepository
}

