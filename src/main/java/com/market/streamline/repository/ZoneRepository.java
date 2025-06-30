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

    boolean existsByStockSymbolAndTimeframeAndCandleTimestampAndZoneTypeAndStrongSwingPoint(
            String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String zoneType, SwingPoint strongSwingPoint
    );

    boolean existsByStrongSwingPointAndZoneType(SwingPoint strongSwingPoint, String zoneType);

    Zone findTopByStockSymbolAndTimeframeAndZoneTypeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
            String stockSymbol, String timeframe, String zoneType, LocalDateTime beforeTimestamp
    );

    Zone findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
            String stockSymbol, String timeframe, LocalDateTime beforeTimestamp
    );

    Zone findTopByStockSymbolAndTimeframeAndIdentifiedAtOrderByIdDesc(String stockSymbol, String timeframe, LocalDateTime identifiedAt);

    // Method 1: Get list of zones of a particular type that satisfy price conditions
    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = 'SUPPLY' " +
            "AND :currentPrice > z.nearPoint AND (z.type IS NULL OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY z.candleTimestamp DESC")
    List<Zone> findSupplyZonesWherePriceAboveNearPoint(@Param("stockSymbol") String stockSymbol,
                                                       @Param("timeframe") String timeframe,
                                                       @Param("currentPrice") Double currentPrice,
                                                       @Param("currentTimestamp") LocalDateTime currentTimestamp);

    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = 'DEMAND' " +
            "AND :currentPrice < z.nearPoint AND (z.type IS NULL OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY z.candleTimestamp DESC")
    List<Zone> findDemandZonesWherePriceBelowNearPoint(@Param("stockSymbol") String stockSymbol,
                                                       @Param("timeframe") String timeframe,
                                                       @Param("currentPrice") Double currentPrice,
                                                       @Param("currentTimestamp") LocalDateTime currentTimestamp);

    // Method 2: Find closest zones with specific price conditions
    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = 'DEMAND' " +
            "AND :currentPrice >= z.nearPoint AND (z.type IS NULL OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY ABS(z.nearPoint - :currentPrice) ASC LIMIT 1")
    Optional<Zone> findClosestDemandZoneWherePriceAboveOrEqualNearPoint(@Param("stockSymbol") String stockSymbol,
                                                                        @Param("timeframe") String timeframe,
                                                                        @Param("currentPrice") Double currentPrice,
                                                                        @Param("currentTimestamp") LocalDateTime currentTimestamp);

    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = 'SUPPLY' " +
            "AND :currentPrice <= z.nearPoint AND (z.type IS NULL OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY ABS(z.nearPoint - :currentPrice) ASC LIMIT 1")
    Optional<Zone> findClosestSupplyZoneWherePriceBelowOrEqualNearPoint(@Param("stockSymbol") String stockSymbol,
                                                                        @Param("timeframe") String timeframe,
                                                                        @Param("currentPrice") Double currentPrice,
                                                                        @Param("currentTimestamp") LocalDateTime currentTimestamp);

    // Method to find the latest zone by zone type
    @Query("SELECT z FROM Zone z WHERE z.stockSymbol = :stockSymbol AND z.timeframe = :timeframe AND z.zoneType = :zoneType " +
            "AND (z.type IS NULL OR z.type = 'VALID') " +
            "AND z.identifiedAt < :currentTimestamp " +
            "ORDER BY z.identifiedAt DESC LIMIT 1")
    Optional<Zone> findLatestZoneByType(@Param("stockSymbol") String stockSymbol,
                                       @Param("timeframe") String timeframe,
                                       @Param("zoneType") String zoneType,
                                       @Param("currentTimestamp") LocalDateTime currentTimestamp);

}
