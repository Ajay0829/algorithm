package com.market.streamline.extractor;

import com.market.streamline.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StockFeatureExtractor {
    @Autowired private SwingPointExtractor swingPointExtractor;
    @Autowired private TrendExtractor trendExtractor;
    @Autowired private ZoneExtractor zoneExtractor;
    @Autowired private LiquiditySweepExtractor liquiditySweepExtractor;
    @Autowired private BreakOfStructureExtractor breakOfStructureExtractor;
    @Autowired private FundamentalExtractor fundamentalExtractor;

    public StockFeatureDTO extractAll(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, double[] prices, Long weakSwingPointId, Long strongSwingPointId, Double peForward, Double atr, String earningsReleaseSession, LocalDateTime nextEarningsDate) {
        StockFeatureDTO dto = new StockFeatureDTO();
        // Example: combine results from all extractors
        SwingPointDTO swing = swingPointExtractor.extract(stockSymbol, timeframe, candleTimestamp, prices);
        TrendDTO trend = trendExtractor.extract(stockSymbol, timeframe, candleTimestamp, prices);
        ZoneDTO zone = zoneExtractor.extract(stockSymbol, timeframe, candleTimestamp, prices);
        LiquiditySweepDTO liquidity = liquiditySweepExtractor.extract(stockSymbol, timeframe, candleTimestamp, prices);
        BreakOfStructureDTO bos = breakOfStructureExtractor.extract(stockSymbol, timeframe, candleTimestamp, weakSwingPointId, strongSwingPointId);
        FundamentalDTO fundamental = fundamentalExtractor.extract(stockSymbol, peForward, atr, earningsReleaseSession, nextEarningsDate);

        // Populate StockFeatureDTO fields from all DTOs (dummy mapping for illustration)
        dto.stockSymbol = stockSymbol;
        dto.timeframe = timeframe;
        dto.candleTimestamp = candleTimestamp;
        dto.currentSwingHigh = swing.price;
        dto.currentSwingLow = swing.price;
        dto.rsi14 = trend.strength;
        dto.nearestRelevantZoneNearPoint = zone.nearPoint;
        dto.nearestRelevantZoneFarPoint = zone.farPoint;
        dto.nearestRelevantZoneType = zone.type;
        dto.nearbyBuyLiquidityZone = liquidity.price;
        dto.forwardPe = fundamental.peForward;
        dto.atr = fundamental.atr;
        dto.earningsReleaseSession = fundamental.earningsReleaseSession;
        dto.prevNCandleHigh = prices.length > 0 ? prices[0] : null;
        return dto;
    }
}

