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
    Optional<Trade> findFirstByStockSymbolAndTimeframeAndResultAndIsActiveTrue(String stockSymbol, String timeframe, String result);

    boolean existsByStockSymbolAndTimeframeAndResultAndIsActiveTrue(String stockSymbol, String timeframe, String result);

    @Modifying
    @Transactional
    @Query("DELETE FROM Trade t WHERE t.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
