package com.market.streamline.repository;

import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    // Basic CRUD methods are inherited from JpaRepository
    List<Zone> findByStockSymbol(String stockSymbol);

    boolean existsByStockSymbolAndTimeframeAndCandleTimestampAndZoneType(
            String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String zoneType
    );

    boolean existsByStrongSwingPointAndZoneType(SwingPoint strongSwingPoint, String zoneType);

    Zone findTopByStockSymbolAndTimeframeAndIdentifiedAtOrderByIdDesc(String stockSymbol, String timeframe, LocalDateTime identifiedAt);

    // Method to find the latest zone by zone type
    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = :zoneType " +
            "AND (z.type IS NULL OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY z.identifiedAt DESC LIMIT 1")
    Optional<Zone> findLatestZoneByType(@Param("stockSymbol") String stockSymbol,
                                       @Param("timeframe") String timeframe,
                                       @Param("zoneType") String zoneType,
                                       @Param("currentTimestamp") LocalDateTime currentTimestamp);

    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = :zoneType " +
            "AND (z.type IS NULL OR z.type = 'ACTIVE' OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY z.identifiedAt DESC LIMIT 1")
    Optional<Zone> findLatestZoneByTypeActiveOrValid(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("zoneType") String zoneType,
            @Param("currentTimestamp") LocalDateTime currentTimestamp);


    // Method to fetch all zones of a particular type with farPoint price conditions
    // For SUPPLY zones: returns zones where current price > farPoint
    // For DEMAND zones: returns zones where current price < farPoint
    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = :zoneType " +
            "AND (z.type IS NULL OR z.type = 'VALID' OR z.type = 'ACTIVE') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "AND ((:zoneType = 'SUPPLY' AND :currentPrice > z.farPoint) OR " +
            "     (:zoneType = 'DEMAND' AND :currentPrice < z.farPoint)) " +
            "ORDER BY z.candleTimestamp DESC")
    List<Zone> findZonesByTypeWithFarPointPriceCondition(@Param("stockSymbol") String stockSymbol,
                                                         @Param("timeframe") String timeframe,
                                                         @Param("zoneType") String zoneType,
                                                         @Param("currentPrice") Double currentPrice,
                                                         @Param("currentTimestamp") LocalDateTime currentTimestamp);

}
