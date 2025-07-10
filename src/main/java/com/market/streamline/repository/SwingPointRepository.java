package com.market.streamline.repository;

import com.market.streamline.entity.structure.SwingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwingPointRepository extends JpaRepository<SwingPoint, Long> {
    // Basic CRUD methods are inherited from JpaRepository
    List<SwingPoint> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    Optional<SwingPoint> findByStockSymbolAndTimeframeAndSwingTypeAndCandleTimestamp(
        String stockSymbol, String timeframe, String swingType, java.time.LocalDateTime candleTimestamp
    );

    List<SwingPoint> findTop2ByStockSymbolAndTimeframeAndConfirmedTrueOrderByCandleTimestampDescIdDesc(String stockSymbol, String timeframe);

    Optional<SwingPoint> findTopByStockSymbolAndTimeframeAndSwingTypeOrderByCandleTimestampDescIdDesc(
        String stockSymbol, String timeframe, String swingType
    );

    Optional<SwingPoint> findTopByStockSymbolAndTimeframeOrderByCandleTimestampDescIdDesc(String stockSymbol, String timeframe);

    @Modifying
    @Transactional
    @Query("DELETE FROM SwingPoint s WHERE s.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
