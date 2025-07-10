package com.market.streamline.repository;

import com.market.streamline.entity.structure.MarketIndicators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MarketIndicatorsRepository extends JpaRepository<MarketIndicators, Long> {
    MarketIndicators findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);

    @Modifying
    @Transactional
    @Query("DELETE FROM MarketIndicators m WHERE m.stockSymbol = :stockSymbol")
    void deleteByStockSymbolInBatch(String stockSymbol);
}
