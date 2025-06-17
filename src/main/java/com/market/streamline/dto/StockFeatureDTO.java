package com.market.streamline.dto;

import java.time.LocalDateTime;

public class StockFeatureDTO {
    public String stockSymbol;
    public String timeframe;
    public LocalDateTime candleTimestamp;
    public Double high;
    public Double low;
    public Double open;
    public Double close;
    public Double volume;
    public Double currentSwingHigh;
    public Double currentSwingLow;
    public Double currentPriceRelativeValue;
    public Double currentCandleDistanceFromLatestSwing;
    public Double prevNCandleHigh;
    public Double prevNCandleLow;
    public Double prevNCandleVolume;
    public Double nearestRelevantZoneNearPoint;
    public Double nearestRelevantZoneFarPoint;
    public String nearestRelevantZoneType;
    public Double nearestRelevantZoneStrength;
    public Double nearestRelevantZoneBosVolume;
    public Double nearbySellLiquidityZone;
    public Double nearbyBuyLiquidityZone;
    public Double rsi14;
    public Double ma14;
    public Boolean isEarningsDay;
    public String earningsReleaseSession;
    public Double forwardPe;
    public Double atr;
}

