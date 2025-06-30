package com.market.streamline.dto;

public class ChartLiquidityDTO {
    private String type; // "liquidity"
    private String action; // "created", "updated", "deleted", "swept"
    private LiquidityData data;

    public ChartLiquidityDTO() {}

    public ChartLiquidityDTO(String type, String action, LiquidityData data) {
        this.type = type;
        this.action = action;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public LiquidityData getData() { return data; }
    public void setData(LiquidityData data) { this.data = data; }

    public static class LiquidityData {
        private String timeframe; // e.g. "1d"
        private String stockSymbol; // e.g. "KO"
        private String liquidityType; // "SELL_SWEEP", "BUY_SWEEP"
        private double price; // Liquidity price level
        private int strength; // Liquidity strength
        private String candleTimestamp; // When the liquidity candle occurred

        public LiquidityData() {}

        public LiquidityData(String timeframe, String stockSymbol, String liquidityType,
                           double price, int strength, String candleTimestamp) {
            this.timeframe = timeframe;
            this.stockSymbol = stockSymbol;
            this.liquidityType = liquidityType;
            this.price = price;
            this.strength = strength;
            this.candleTimestamp = candleTimestamp;
        }

        // Getters and Setters
        public String getTimeframe() { return timeframe; }
        public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
        public String getStockSymbol() { return stockSymbol; }
        public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }
        public String getLiquidityType() { return liquidityType; }
        public void setLiquidityType(String liquidityType) { this.liquidityType = liquidityType; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getStrength() { return strength; }
        public void setStrength(int strength) { this.strength = strength; }
        public String getCandleTimestamp() { return candleTimestamp; }
        public void setCandleTimestamp(String candleTimestamp) { this.candleTimestamp = candleTimestamp; }
    }
}
