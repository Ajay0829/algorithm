package com.market.streamline.repository;

import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.SwingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakOfStructureRepository extends JpaRepository<BreakOfStructure, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    List<BreakOfStructure> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    boolean existsByWeakSwingPointAndStrongSwingPoint(SwingPoint swingPoint, SwingPoint weakSwingPoint);
}
