package com.market.analysis.technical.model;

import com.market.external.polygon.dto.Candle;

public interface MarketZone {
    Candle getCandle();
    ZoneType getZoneType();

    enum ZoneType {
        SUPPLY,
        DEMAND
    }
}
