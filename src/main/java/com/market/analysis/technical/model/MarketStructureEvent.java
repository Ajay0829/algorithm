package com.market.analysis.technical.model;

import com.market.common.SwingPoint;

public class MarketStructureEvent {
    public enum EventType {
        BREAK_OF_STRUCTURE,
        LIQUIDITY_SWEEP
    }

    private final EventType eventType;
    private final MarketTrend marketTrend;
    private final SwingPoint swingPoint;
    private final MarketEventDetail eventDetail;

    public MarketStructureEvent(EventType eventType, MarketTrend marketTrend, SwingPoint swingPoint, MarketEventDetail eventDetail) {
        this.eventType = eventType;
        this.marketTrend = marketTrend;
        this.swingPoint = swingPoint;
        this.eventDetail = eventDetail;
    }

    public EventType getEventType() {
        return eventType;
    }

    public MarketTrend getMarketTrend() {
        return marketTrend;
    }

    public SwingPoint getSwingPoint() {
        return swingPoint;
    }

    public MarketEventDetail getEventDetail() {
        return eventDetail;
    }
}

