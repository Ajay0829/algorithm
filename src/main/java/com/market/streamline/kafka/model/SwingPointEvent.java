package com.market.streamline.kafka.model;

import com.market.streamline.entity.structure.SwingPoint;

public class SwingPointEvent {
    private String stockSymbol;
    private String timeframe;
    private String candleTimestamp;
    private String swingType;
    private double price;
    private boolean confirmed;
    private boolean isMajor;


    public SwingPointEvent() {
    }

    public SwingPointEvent(String stockSymbol, String timeframe, String candleTimestamp, String swingType, double price, boolean confirmed, boolean isMajor) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.swingType = swingType;
        this.price = price;
        this.confirmed = confirmed;
        this.isMajor = isMajor;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getCandleTimestamp() {
        return candleTimestamp;
    }

    public void setCandleTimestamp(String candleTimestamp) {
        this.candleTimestamp = candleTimestamp;
    }

    public String getSwingType() {
        return swingType;
    }

    public void setSwingType(String swingType) {
        this.swingType = swingType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isMajor() {
        return isMajor;
    }

    public void setMajor(boolean major) {
        isMajor = major;
    }

    public static SwingPointEvent fromSwingPoint(SwingPoint swingPoint) {
        return new SwingPointEvent(
            swingPoint.getStockSymbol(),
            swingPoint.getTimeframe(),
            swingPoint.getCandleTimestamp().toString(),
            swingPoint.getSwingType(),
            swingPoint.getPrice(),
            swingPoint.getConfirmed(),
            swingPoint.getIsMajor()
        );
    }
}
