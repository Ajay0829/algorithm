package com.market.analysis.technical.service;

import com.market.analysis.technical.model.DemandZone;
import com.market.analysis.technical.model.SupplyZone;
import com.market.analysis.technical.model.MarketStructureEvent;
import com.market.analysis.technical.model.BreakOfStructure;
import com.market.common.SwingPoint;
import com.market.external.polygon.dto.Candle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ZoneDetectionService {

    private static final double DEFAULT_DRAW_DOWN_FRACTION = 0.05; // Default fraction for demand/supply zone detection
    private static final double BREAK_THRESHOLD_PCT = 0.05; // 5% threshold for break detection

    public List<DemandZone> detectDemandZones(List<Candle> candles, List<SwingPoint> swingPoints, List<MarketStructureEvent> events) {
        return detectZones(candles, events, true);
    }

    public List<SupplyZone> detectSupplyZones(List<Candle> candles, List<SwingPoint> swingPoints, List<MarketStructureEvent> events) {
        return detectZones(candles, events, false);
    }

    private <T> List<T> detectZones(List<Candle> candles, List<MarketStructureEvent> events, boolean isDemand) {
        List<T> zones = new java.util.ArrayList<>();
        for (MarketStructureEvent event : events) {
            if (event.getEventType() == MarketStructureEvent.EventType.BREAK_OF_STRUCTURE &&
                ((isDemand && event.getMarketTrend() == com.market.analysis.technical.model.MarketTrend.UPTREND) ||
                 (!isDemand && event.getMarketTrend() == com.market.analysis.technical.model.MarketTrend.DOWNTREND))) {
                T zone = detectSingleZone(candles, event, isDemand);
                if (zone != null) {
                    zones.add(zone);
                }
            }
        }
        return zones;
    }

    @SuppressWarnings("unchecked")
    private <T> T detectSingleZone(List<Candle> candles, MarketStructureEvent event, boolean isDemand) {
        BreakOfStructure bos = (BreakOfStructure) event.getEventDetail();
        if (!validateAndReturnNullIfSwingPointsValid(candles, bos.getLeft(), bos.getMiddle(), bos.getRight())) {
            return null;
        }
        int leftIdx = bos.getLeft().getIndex();
        int middleIdx = bos.getMiddle().getIndex();
        Optional<Integer> findBreakIdx = findBreakIdx(candles, leftIdx, middleIdx, isDemand);
        if (!findBreakIdx.isPresent()) {
            return null;
        }
        int breakIdx = findBreakIdx.get();
        int zoneIdx = findFarthestImpulseStartIdx(candles, middleIdx, breakIdx, isDemand);
        if (zoneIdx == -1) zoneIdx = middleIdx;
        Candle zoneCandle = candles.get(zoneIdx);
        if (isDemand) {
            return (T) new DemandZone(zoneCandle);
        } else {
            return (T) new SupplyZone(zoneCandle);
        }
    }

    private boolean validateAndReturnNullIfSwingPointsValid(List<Candle> candles, SwingPoint left, SwingPoint middle, SwingPoint right) {
        if (left == null || middle == null || right == null) return false;
        int leftIdx = left.getIndex();
        int middleIdx = middle.getIndex();
        int rightIdx = right.getIndex();
        int size = candles.size();
        return !(leftIdx < 0 || middleIdx < 0 || rightIdx < 0 || leftIdx >= size || middleIdx >= size || rightIdx >= size);
    }

    private Optional<Integer> findBreakIdx(List<Candle> candles, int leftIdx, int middleIdx, boolean isDemand) {
        if (candles == null || candles.isEmpty() || leftIdx >= candles.size()) return Optional.empty();
        double threshold = isDemand ? candles.get(leftIdx).getHigh() * (1 + BREAK_THRESHOLD_PCT)
                : candles.get(leftIdx).getLow() * (1 - BREAK_THRESHOLD_PCT);
        for (int i = middleIdx + 1; i < candles.size(); i++) {
            if ((isDemand && candles.get(i).getHigh() > threshold) || (!isDemand && candles.get(i).getLow() < threshold)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private int findFarthestImpulseStartIdx(List<Candle> candles, int middleIdx, int breakIdx, boolean isDemand) {
        if (candles == null || candles.isEmpty() || middleIdx >= breakIdx) {
            return -1;
        }
        double middleClose = candles.get(middleIdx).getClose();
        double breakClose = candles.get(breakIdx).getClose();
        double breakoutPct = isDemand
            ? (breakClose - middleClose) / middleClose
            : (middleClose - breakClose) / middleClose;
        if (breakoutPct <= 0) {
            return -1;
        }
        double allowedDrawDown = breakoutPct * DEFAULT_DRAW_DOWN_FRACTION;
        int lastValidIdx = -1;
        for (int i = breakIdx - 1; i >= middleIdx; i--) {
            for (int j = i + 1; j <= breakIdx; j++) {
                double prevClose = candles.get(j - 1).getClose();
                double currClose = candles.get(j).getClose();
                double pctChange = isDemand
                    ? (currClose - prevClose) / prevClose
                    : (prevClose - currClose) / prevClose;
                if (pctChange < -allowedDrawDown) {
                    return lastValidIdx;
                }
            }
            lastValidIdx = i;
        }
        return lastValidIdx;
    }
}
