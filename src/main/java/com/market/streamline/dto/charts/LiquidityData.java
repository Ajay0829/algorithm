package com.market.streamline.dto.charts;

public class LiquidityData {
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
