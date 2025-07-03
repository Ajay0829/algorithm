package com.market.streamline.repository;

import com.market.streamline.entity.structure.Volatility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolatilityRepository extends JpaRepository<Volatility, Long> {
    Volatility findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);
}
