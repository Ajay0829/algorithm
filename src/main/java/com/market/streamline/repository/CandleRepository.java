package com.market.streamline.repository;

import com.market.streamline.entity.CandleEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Long> {
    @Query("SELECT c FROM CandleEntity c WHERE c.stockSymbol = :stockSymbol AND c.timeframe = :timeframe AND c.candleTimestamp <= :beforeTimestamp ORDER BY c.candleTimestamp DESC")
    List<CandleEntity> findRecentCandles(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("beforeTimestamp") LocalDateTime beforeTimestamp,
            Pageable pageable
    );

    List<CandleEntity> findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    // Find the most recent candle before or at the given timestamp
    CandleEntity findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanEqualOrderByCandleTimestampDesc(
        String stockSymbol, String timeframe, LocalDateTime timestamp
    );

    CandleEntity findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
            String stockSymbol, String timeframe, LocalDateTime timestamp
    );

    CandleEntity findTopByStockSymbolAndTimeframeAndCandleTimestampOrderByCandleTimestampDesc(
        String stockSymbol, String timeframe, LocalDateTime timestamp
    );

    // Find the earliest candle after the given timestamp
    CandleEntity findTopByStockSymbolAndTimeframeAndCandleTimestampGreaterThanOrderByCandleTimestampAsc(
        String stockSymbol, String timeframe, LocalDateTime timestamp
    );

    boolean existsByStockSymbolAndTimeframe(String stockSymbol, String timeframe);
}
