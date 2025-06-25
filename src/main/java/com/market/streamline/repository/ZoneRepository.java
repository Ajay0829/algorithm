package com.market.streamline.repository;

import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    // Basic CRUD methods are inherited from JpaRepository
    List<Zone> findByStockSymbol(String stockSymbol);

    boolean existsByStockSymbolAndTimeframeAndCandleTimestampAndZoneType(
            String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String zoneType
    );

    boolean existsByStockSymbolAndTimeframeAndCandleTimestampAndZoneTypeAndStrongSwingPoint(
            String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String zoneType, SwingPoint strongSwingPoint
    );

    boolean existsByStrongSwingPointAndZoneType(SwingPoint strongSwingPoint, String zoneType);
}
