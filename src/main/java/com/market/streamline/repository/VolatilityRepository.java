package com.market.streamline.repository;

import com.market.streamline.entity.Volatility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VolatilityRepository extends JpaRepository<Volatility, Long> {
    Volatility findByStockSymbolAndTimeframe(String stockSymbol, String timeframe);
}
