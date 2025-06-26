package com.market.streamline.repository;

import com.market.streamline.entity.Trend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrendRepository extends JpaRepository<Trend, Long> {
    // Basic CRUD methods are inherited from JpaRepository

    Trend findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
        String stockSymbol,
        String timeframe
    );

    List<Trend> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);
}
