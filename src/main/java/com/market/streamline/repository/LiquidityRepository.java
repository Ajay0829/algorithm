package com.market.streamline.repository;

import com.market.streamline.entity.liquidity.Liquidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidityRepository extends JpaRepository<Liquidity, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    // Using Spring Data JPA method naming convention with First to get only one result
    Optional<Liquidity> findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            String stockSymbol, String timeframe, String liquidityType);

    @Query("SELECT l FROM Liquidity l WHERE l.stockSymbol = :stockSymbol " +
           "AND l.timeframe = :timeframe AND l.liquidityType = :liquidityType " +
           "AND l.price < :currentPrice " +
           "ORDER BY l.candleTimestamp DESC")
    List<Liquidity> findLiquiditiesBelowPrice(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("liquidityType") String liquidityType,
            @Param("currentPrice") Double currentPrice);

    @Query("SELECT l FROM Liquidity l WHERE l.stockSymbol = :stockSymbol " +
           "AND l.timeframe = :timeframe AND l.liquidityType = :liquidityType " +
           "AND l.price > :currentPrice " +
           "ORDER BY l.candleTimestamp DESC")
    List<Liquidity> findLiquiditiesAbovePrice(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("liquidityType") String liquidityType,
            @Param("currentPrice") Double currentPrice);
}
