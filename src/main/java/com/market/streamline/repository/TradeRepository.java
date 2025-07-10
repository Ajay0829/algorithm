package com.market.streamline.repository;

import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Find any active trade for a specific stock symbol and timeframe
    Optional<Trade> findFirstByStockSymbolAndTimeframeAndIsActiveTrue(String stockSymbol, String timeframe);

    Optional<Trade> findByStockSymbolAndTimeframeAndZoneAndIsActiveTrue(String stockSymbol, String timeframe, Zone zone);

    boolean existsByStockSymbolAndTimeframeAndIsActiveTrue(String stockSymbol, String timeframe);

    // OPTIMIZATION: Batch query method to avoid N+1 query problem in fillTradeInformation
    List<Trade> findByStockSymbolAndTimeframeAndTimestampIn(String stockSymbol, String timeframe, List<LocalDateTime> timestamps);

    @Modifying
    @Transactional
    @Query("DELETE FROM Trade t WHERE t.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
