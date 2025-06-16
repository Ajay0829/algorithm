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
            SwingPoint first = swingPoints.get(i - 2);
            SwingPoint middle = swingPoints.get(i - 1);
            SwingPoint last = swingPoints.get(i);
            if (isBullishStructure(first, middle, last, thresholdPercent)) {
                events.add(createBullishBOS(first));
            } else if (isBullishLiquiditySweep(first, middle, last, thresholdPercent)) {
                events.add(createBullishLiquiditySweep(first));
            } else if (isBearishStructure(first, middle, last, thresholdPercent)) {
                events.add(createBearishBOS(first));
            } else if (isBearishLiquiditySweep(first, middle, last, thresholdPercent)) {
                events.add(createBearishLiquiditySweep(first));
            }
        }
        return events;
    }

    private boolean isBullishStructure(SwingPoint first, SwingPoint middle, SwingPoint last, double thresholdPercent) {
        return first.isHigh() && middle.isLow() && last.isHigh()
                && last.getPrice() > first.getPrice()
                && calculatePercentageDifference(last.getPrice(), first.getPrice()) > thresholdPercent;
    }

    private boolean isBullishLiquiditySweep(SwingPoint first, SwingPoint middle, SwingPoint last, double thresholdPercent) {
        return first.isHigh() && middle.isLow() && last.isHigh()
                && last.getPrice() > first.getPrice()
                && calculatePercentageDifference(last.getPrice(), first.getPrice()) > 0
                && calculatePercentageDifference(last.getPrice(), first.getPrice()) <= thresholdPercent;
    }

    private boolean isBearishStructure(SwingPoint first, SwingPoint middle, SwingPoint last, double thresholdPercent) {
        return first.isLow() && middle.isHigh() && last.isLow()
                && last.getPrice() < first.getPrice()
                && Math.abs(calculatePercentageDifference(last.getPrice(), first.getPrice())) > thresholdPercent;
    }

    private boolean isBearishLiquiditySweep(SwingPoint first, SwingPoint middle, SwingPoint last, double thresholdPercent) {
        return first.isLow() && middle.isHigh() && last.isLow()
                && last.getPrice() < first.getPrice()
                && Math.abs(calculatePercentageDifference(last.getPrice(), first.getPrice())) > 0
                && Math.abs(calculatePercentageDifference(last.getPrice(), first.getPrice())) <= thresholdPercent;
    }

    private MarketStructureEvent createBullishBOS(SwingPoint weakHigh) {
        BreakOfStructure bos = new BreakOfStructure(MarketTrend.UPTREND, weakHigh);
        return new MarketStructureEvent(MarketStructureEvent.EventType.BREAK_OF_STRUCTURE, MarketTrend.UPTREND, weakHigh, bos);
    }

    private MarketStructureEvent createBearishBOS(SwingPoint weakLow) {
        BreakOfStructure bos = new BreakOfStructure(MarketTrend.DOWNTREND, weakLow);
        return new MarketStructureEvent(MarketStructureEvent.EventType.BREAK_OF_STRUCTURE, MarketTrend.DOWNTREND, weakLow, bos);
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
