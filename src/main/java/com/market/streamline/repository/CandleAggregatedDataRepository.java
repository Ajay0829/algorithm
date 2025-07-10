package com.market.streamline.repository;

import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CandleAggregatedDataRepository extends JpaRepository<CandleAggregatedDataEntity, Long> {

    @Query("SELECT c FROM CandleAggregatedDataEntity c WHERE c.stockSymbol = :stockSymbol AND c.timeframe = :timeframe ORDER BY c.candleTimestamp ASC")
    List<CandleAggregatedDataEntity> findByStockSymbolAndTimeframeOrderByTimestamp(
            @Param("stockSymbol") String stockSymbol,
            @Param("timeframe") String timeframe
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM CandleAggregatedDataEntity c WHERE c.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
