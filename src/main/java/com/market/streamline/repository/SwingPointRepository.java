package com.market.streamline.repository;

import com.market.streamline.entity.SwingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwingPointRepository extends JpaRepository<SwingPoint, Long> {
    // Basic CRUD methods are inherited from JpaRepository
    List<SwingPoint> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    Optional<SwingPoint> findByStockSymbolAndTimeframeAndSwingTypeAndCandleTimestamp(
        String stockSymbol, String timeframe, String swingType, java.time.LocalDateTime candleTimestamp
    );
}
