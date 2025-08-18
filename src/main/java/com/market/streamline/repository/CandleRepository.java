package com.market.streamline.repository;

import com.market.streamline.entity.structure.CandleEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    CandleEntity findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
            String stockSymbol, String timeframe, LocalDateTime timestamp
    );

    CandleEntity findTopByStockSymbolAndTimeframeAndCandleTimestampOrderByCandleTimestampDesc(
        String stockSymbol, String timeframe, LocalDateTime timestamp
    );

    @Query("SELECT COUNT(c) FROM CandleEntity c WHERE c.stockSymbol = :stockSymbol AND c.timeframe = :timeframe AND c.candleTimestamp >= :startTimestamp AND c.candleTimestamp <= :endTimestamp")
    Long countCandlesBetweenTimestamps(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("startTimestamp") LocalDateTime startTimestamp,
            @Param("endTimestamp") LocalDateTime endTimestamp
    );

    @Query("SELECT c FROM CandleEntity c WHERE c.stockSymbol = :stockSymbol AND c.timeframe = :timeframe AND c.candleTimestamp >= :startTimestamp AND c.candleTimestamp <= :endTimestamp")
    List<CandleEntity> getCandlesBetweenTimestamps(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("startTimestamp") LocalDateTime startTimestamp,
            @Param("endTimestamp") LocalDateTime endTimestamp
    );

    @Query("SELECT c FROM CandleEntity c WHERE c.stockSymbol = :stockSymbol AND c.timeframe = :timeframe ORDER BY c.candleTimestamp DESC LIMIT :number")
    List<CandleEntity> getRecentCandlesByNumber(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe,
            @Param("number") Long number
    );


    @Modifying
    @Transactional
    @Query("DELETE FROM CandleEntity c WHERE c.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);

    long countByStockSymbolAndTimeframeAndCandleTimestampBetween(
        String stockSymbol, String timeframe, LocalDateTime start, LocalDateTime end
    );
}
