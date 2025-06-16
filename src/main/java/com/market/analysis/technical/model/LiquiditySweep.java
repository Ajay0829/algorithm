package com.market.analysis.technical.model;

import com.market.common.SwingPoint;

public class LiquiditySweep implements MarketEventDetail {
    private final MarketTrend marketTrend;
    private final SwingPoint swingPoint;
    private final LiquiditySweepType type;

    public enum LiquiditySweepType {
        SELL_SWEEP,
        BUY_SWEEP
    }

    public LiquiditySweep(MarketTrend marketTrend, SwingPoint swingPoint, LiquiditySweepType type) {
        this.marketTrend = marketTrend;
        this.swingPoint = swingPoint;
        this.type = type;
    }

    public MarketTrend getMarketTrend() {
        return marketTrend;
    }

    public SwingPoint getSwingPoint() {
        return swingPoint;
    }

    public LiquiditySweepType getType() {
        return type;
    }
}
