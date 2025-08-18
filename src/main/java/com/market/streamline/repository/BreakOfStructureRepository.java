package com.market.streamline.repository;

import com.market.streamline.entity.structure.BreakOfStructure;
import com.market.streamline.entity.structure.SwingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BreakOfStructureRepository extends JpaRepository<BreakOfStructure, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    List<BreakOfStructure> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    boolean existsByWeakSwingPointAndStrongSwingPoint(SwingPoint swingPoint, SwingPoint weakSwingPoint);

    Optional<BreakOfStructure> findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(String stockSymbol, String timeframe);

    @Modifying
    @Transactional
    @Query("DELETE FROM BreakOfStructure b WHERE b.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
