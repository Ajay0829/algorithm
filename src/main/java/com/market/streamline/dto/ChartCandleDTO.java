package com.market.streamline.dto;

public class ChartCandleDTO {
    private String type; // "candle"
    private String action; // "created", "updated", "deleted"
    private CandleData data;

    public ChartCandleDTO() {}

    public ChartCandleDTO(String type, String action, CandleData data) {
        this.type = type;
        this.action = action;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public CandleData getData() { return data; }
    public void setData(CandleData data) { this.data = data; }

    public static class CandleData {
        private String timestamp;
        private double open;
        private double high;
        private double low;
        private double close;
        private double volume;
        private String stockSymbol;
        private String timeframe;

        public CandleData() {}

        public CandleData(String timestamp, double open, double high, double low, double close, double volume, String stockSymbol, String timeframe) {
            this.timestamp = timestamp;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
            this.stockSymbol = stockSymbol;
            this.timeframe = timeframe;
        }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
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
        public String getStockSymbol() { return stockSymbol; }
        public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }
        public String getTimeframe() { return timeframe; }
        public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    }
}

