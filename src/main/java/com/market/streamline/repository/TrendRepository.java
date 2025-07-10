package com.market.streamline.repository;

import com.market.streamline.entity.structure.Trend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TrendRepository extends JpaRepository<Trend, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    Trend findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
        String stockSymbol,
        String timeframe
    );

    List<Trend> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    @Modifying
    @Transactional
    @Query("DELETE FROM Trend t WHERE t.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
