package com.market.streamline.repository;

import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.SwingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BreakOfStructureRepository extends JpaRepository<BreakOfStructure, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    List<BreakOfStructure> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    boolean existsByWeakSwingPointAndStrongSwingPoint(SwingPoint swingPoint, SwingPoint weakSwingPoint);

    Optional<BreakOfStructure> findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(String stockSymbol, String timeframe);

    Optional<BreakOfStructure> findByStockSymbolAndTimeframeAndCandleTimestamp(String stockSymbol, String timeframe, java.time.LocalDateTime candleTimestamp);

    Optional<BreakOfStructure> findByStockSymbolAndTimeframeAndCandleTimestampAndDirection(String stockSymbol, String timeframe, java.time.LocalDateTime candleTimestamp, String direction);

    List<BreakOfStructure> findByStockSymbolAndTimeframeAndCandleTimestampBetweenOrderByCandleTimestampDesc(
            String stockSymbol, String timeframe, LocalDateTime from, LocalDateTime to);
}
