package com.market.streamline.model;

public class CandleEvent {
    private String stockSymbol;
    private String timeframe;
    private String candleTimestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    public CandleEvent(String stockSymbol, String timeframe, String candleTimestamp, double open, double high, double low, double close, double volume) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public CandleEvent() {

    }

    // Getters and setters
    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public String getCandleTimestamp() { return candleTimestamp; }
    public void setCandleTimestamp(String candleTimestamp) { this.candleTimestamp = candleTimestamp; }
    public double getOpen() { return open; }
    public void setOpen(double open) { this.open = open; }
    public double getHigh() { return high; }
    public void setHigh(double high) { this.high = high; }
    public double getLow() { return low; }
    public void setLow(double low) { this.low = low; }
    public double getClose() { return close; }
    public void setClose(double close) { this.close = close; }
    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }
}

