package com.market.analysis.technical.model;

import com.market.external.polygon.dto.Candle;

public class DemandZone implements MarketZone {
    private final Candle candle;
    private final ZoneType zoneType;

    public DemandZone(Candle candle) {
        this.candle = candle;
        this.zoneType = ZoneType.DEMAND;
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
