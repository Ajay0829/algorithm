package com.market.streamline.repository;

import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // Find active trades for a specific stock symbol and timeframe
    List<Trade> findByStockSymbolAndTimeframeAndIsActiveTrue(String stockSymbol, String timeframe);

    // Find any active trade for a specific stock symbol and timeframe
    Optional<Trade> findFirstByStockSymbolAndTimeframeAndIsActiveTrue(String stockSymbol, String timeframe);

    // Method to check if ANY trade exists at a specific timestamp (regardless of active status)
    Optional<Trade> findFirstByStockSymbolAndTimeframeAndTimestamp(String stockSymbol, String timeframe, LocalDateTime timestamp);

    Optional<Trade> findByStockSymbolAndTimeframeAndZone(String stockSymbol, String timeframe, Zone zone);

    boolean existsByStockSymbolAndTimeframeAndIsActiveTrue(String stockSymbol, String timeframe);
}
