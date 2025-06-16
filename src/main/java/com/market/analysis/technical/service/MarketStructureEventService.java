package com.market.analysis.technical.service;

import com.market.analysis.technical.model.BreakOfStructure;
import com.market.analysis.technical.model.MarketTrend;
import com.market.analysis.technical.model.MarketStructureEvent;
import com.market.analysis.technical.model.LiquiditySweep;
import com.market.common.SwingPoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MarketStructureEventService {

    private static final double DEFAULT_BOS_THRESHOLD_PERCENT = 5;

    public List<MarketStructureEvent> detectMarketStructureEvents(List<SwingPoint> swingPoints) {
        return detectMarketStructureEvents(swingPoints, DEFAULT_BOS_THRESHOLD_PERCENT);
    }

    public List<MarketStructureEvent> detectMarketStructureEvents(List<SwingPoint> swingPoints, double thresholdPercent) {
        if (swingPoints == null || swingPoints.size() < 3) {
            return Collections.emptyList();
        }
        List<MarketStructureEvent> events = new ArrayList<>();
        for (int i = 2; i < swingPoints.size(); i++) {
            SwingPoint left = swingPoints.get(i - 2);
            SwingPoint middle = swingPoints.get(i - 1);
            SwingPoint right = swingPoints.get(i);
            if (isBullishStructure(left, middle, right, thresholdPercent)) {
                events.add(createBullishBOS(left, middle, right));
            } else if (isBullishLiquiditySweep(left, middle, right, thresholdPercent)) {
                events.add(createBullishLiquiditySweep(left));
            } else if (isBearishStructure(left, middle, right, thresholdPercent)) {
                events.add(createBearishBOS(left, middle, right));
            } else if (isBearishLiquiditySweep(left, middle, right, thresholdPercent)) {
                events.add(createBearishLiquiditySweep(left));
            }
        }
        return events;
    }

    private boolean isBullishStructure(SwingPoint left, SwingPoint middle, SwingPoint right, double thresholdPercent) {
        return left.isHigh() && middle.isLow() && right.isHigh()
                && right.getPrice() > left.getPrice()
                && calculatePercentageDifference(right.getPrice(), left.getPrice()) > thresholdPercent;
    }

    private boolean isBullishLiquiditySweep(SwingPoint left, SwingPoint middle, SwingPoint right, double thresholdPercent) {
        return left.isHigh() && middle.isLow() && right.isHigh()
                && right.getPrice() > left.getPrice()
                && calculatePercentageDifference(right.getPrice(), left.getPrice()) > 0
                && calculatePercentageDifference(right.getPrice(), left.getPrice()) <= thresholdPercent;
    }

    private boolean isBearishStructure(SwingPoint left, SwingPoint middle, SwingPoint right, double thresholdPercent) {
        return left.isLow() && middle.isHigh() && right.isLow()
                && right.getPrice() < left.getPrice()
                && Math.abs(calculatePercentageDifference(right.getPrice(), left.getPrice())) > thresholdPercent;
    }

    private boolean isBearishLiquiditySweep(SwingPoint left, SwingPoint middle, SwingPoint right, double thresholdPercent) {
        return left.isLow() && middle.isHigh() && right.isLow()
                && right.getPrice() < left.getPrice()
                && Math.abs(calculatePercentageDifference(right.getPrice(), left.getPrice())) > 0
                && Math.abs(calculatePercentageDifference(right.getPrice(), left.getPrice())) <= thresholdPercent;
    }

    private MarketStructureEvent createBullishBOS(SwingPoint left, SwingPoint middle, SwingPoint right) {
        BreakOfStructure bos = new BreakOfStructure(MarketTrend.UPTREND, left, middle, right);
        return new MarketStructureEvent(MarketStructureEvent.EventType.BREAK_OF_STRUCTURE, MarketTrend.UPTREND, left, bos);
    }

    private MarketStructureEvent createBearishBOS(SwingPoint left, SwingPoint middle, SwingPoint right) {
        BreakOfStructure bos = new BreakOfStructure(MarketTrend.DOWNTREND, left, middle, right);
        return new MarketStructureEvent(MarketStructureEvent.EventType.BREAK_OF_STRUCTURE, MarketTrend.DOWNTREND, left, bos);
    }

    private MarketStructureEvent createBullishLiquiditySweep(SwingPoint weakHigh) {
        LiquiditySweep sweep = new LiquiditySweep(MarketTrend.UPTREND, weakHigh, LiquiditySweep.LiquiditySweepType.SELL_SWEEP);
        return new MarketStructureEvent(MarketStructureEvent.EventType.LIQUIDITY_SWEEP, MarketTrend.UPTREND, weakHigh, sweep);
    }

    private MarketStructureEvent createBearishLiquiditySweep(SwingPoint weakLow) {
        LiquiditySweep sweep = new LiquiditySweep(MarketTrend.DOWNTREND, weakLow, LiquiditySweep.LiquiditySweepType.BUY_SWEEP);
        return new MarketStructureEvent(MarketStructureEvent.EventType.LIQUIDITY_SWEEP, MarketTrend.DOWNTREND, weakLow, sweep);
    }

    private double calculatePercentageDifference(double newValue, double oldValue) {
        if (oldValue == 0) return 0;
        return ((newValue - oldValue) / oldValue) * 100.0;
    }
}
