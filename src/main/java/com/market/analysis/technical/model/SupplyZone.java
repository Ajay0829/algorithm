package com.market.analysis.technical.model;

import com.market.external.polygon.dto.Candle;

public class SupplyZone implements MarketZone {
    private final Candle candle;
    private final ZoneType zoneType;

    public SupplyZone(Candle candle) {
        this.candle = candle;
        this.zoneType = ZoneType.SUPPLY;
    }

    @Override
    public Candle getCandle() {
        return candle;
    }

    @Override
    public ZoneType getZoneType() {
        return zoneType;
    }
}
