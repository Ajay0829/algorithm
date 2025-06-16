package com.market.analysis.technical.model;

import com.market.common.SwingPoint;

public class BreakOfStructure implements MarketEventDetail {
    private final MarketTrend marketTrend;
    private final SwingPoint weakSwingPoint;

    public BreakOfStructure(MarketTrend marketTrend, SwingPoint weakSwingPoint) {
        this.marketTrend = marketTrend;
        this.weakSwingPoint = weakSwingPoint;
    }

    public MarketTrend getMarketTrend() {
        return marketTrend;
    }

    public SwingPoint getWeakSwingPoint() {
        return weakSwingPoint;
    }
}
