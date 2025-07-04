package com.market.streamline.repository;

import com.market.streamline.entity.structure.MarketIndicators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketIndicatorsRepository extends JpaRepository<MarketIndicators, Long> {
    MarketIndicators findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);
}
