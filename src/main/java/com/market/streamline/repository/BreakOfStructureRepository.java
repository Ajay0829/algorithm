package com.market.streamline.repository;

import com.market.streamline.entity.BreakOfStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreakOfStructureRepository extends JpaRepository<BreakOfStructure, Long> {
    // Basic CRUD methods are inherited from JpaRepository
}

