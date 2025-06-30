package com.market.streamline.repository;

import com.market.streamline.entity.LiquiditySweep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiquiditySweepRepository extends JpaRepository<LiquiditySweep, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    /**
     * Finds the latest liquidity sweep for a given stock symbol and timeframe
     * @param stockSymbol the stock symbol to search for
     * @param timeframe the timeframe to search for
     * @return Optional containing the latest liquidity sweep if found
     */
    @Query("SELECT ls FROM LiquiditySweep ls WHERE ls.stockSymbol = :stockSymbol AND ls.timeframe = :timeframe ORDER BY ls.candleTimestamp DESC LIMIT 1")
    Optional<LiquiditySweep> findLatestByStockSymbolAndTimeframe(@Param("stockSymbol") String stockSymbol, @Param("timeframe") String timeframe);
}
