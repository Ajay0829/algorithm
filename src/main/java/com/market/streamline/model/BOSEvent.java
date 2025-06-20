package com.market.streamline.model;

import com.market.streamline.entity.SwingPoint;

import java.time.LocalDateTime;

public class BOSEvent {
    private String stockSymbol;
    private String timeframe;
    private String direction;
    private LocalDateTime candleTimestamp;
    private SwingPoint swingPoint;

    public BOSEvent() {

    }

    public BOSEvent(String stockSymbol, String timeframe, String direction, LocalDateTime candleTimestamp, SwingPoint swingPoint) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.direction = direction;
        this.candleTimestamp = candleTimestamp;
        this.swingPoint = swingPoint;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public String getDirection() {
        return direction;
    }

    public LocalDateTime getCandleTimestamp() {
        return candleTimestamp;
    }

    public SwingPoint getSwingPoint() {
        return swingPoint;
    }
}
