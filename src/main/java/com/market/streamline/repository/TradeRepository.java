package com.market.streamline.repository;

import com.market.streamline.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // You can add custom query methods here if needed
}

