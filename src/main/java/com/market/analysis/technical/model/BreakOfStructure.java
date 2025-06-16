package com.market.analysis.technical.model;

import com.market.common.SwingPoint;

public class BreakOfStructure implements MarketEventDetail {
    private final MarketTrend marketTrend;
    private final SwingPoint left;
    private final SwingPoint middle;
    private final SwingPoint right;

    public BreakOfStructure(MarketTrend marketTrend, SwingPoint left, SwingPoint middle, SwingPoint right) {
        this.marketTrend = marketTrend;
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public SwingPoint getLeft() {
        return left;
    }

    public SwingPoint getMiddle() {
        return middle;
    }

    public SwingPoint getRight() {
        return right;
    }

    public MarketTrend getMarketTrend() {
        return marketTrend;
    }
}
