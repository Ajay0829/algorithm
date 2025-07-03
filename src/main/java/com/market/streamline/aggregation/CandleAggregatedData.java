package com.market.streamline.aggregation;

import java.time.LocalDateTime;

public class CandleAggregatedData {
    // Basic candle data
    private String stockSymbol;
    private String timeframe;
    private LocalDateTime candleTimestamp;
    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private Double volume;

    private double lastSwingHigh;
    private double lastSwingLow;

    // Break of structure data
    private double supplyPrice;
    private double demandPrice;


    private int lastLiquiditySweepType;
    private int bosDirection;

    private double buyLiquidity;
    private double sellLiquidity;

    // Trend data
    private double volatility;
    private String trade;

    // Trade data
    private double entryPrice;

    // Volatility data
    private double targetPrice;

    // Feature data
    private double stopLossPrice;

    private boolean tradeResult;

    // Constructors
    public CandleAggregatedData() {}

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

    public LocalDateTime getCandleTimestamp() {
        return candleTimestamp;
    }

    public void setCandleTimestamp(LocalDateTime candleTimestamp) {
        this.candleTimestamp = candleTimestamp;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public double getLastSwingHigh() {
        return lastSwingHigh;
    }

    public void setLastSwingHigh(double lastSwingHigh) {
        this.lastSwingHigh = lastSwingHigh;
    }

    public double getLastSwingLow() {
        return lastSwingLow;
    }

    public void setLastSwingLow(double lastSwingLow) {
        this.lastSwingLow = lastSwingLow;
    }

    public double getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(double supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public double getDemandPrice() {
        return demandPrice;
    }

    public void setDemandPrice(double demandPrice) {
        this.demandPrice = demandPrice;
    }

    public int isLastLiquiditySweepType() {
        return lastLiquiditySweepType;
    }

    public void setLastLiquiditySweepType(int lastLiquiditySweepType) {
        this.lastLiquiditySweepType = lastLiquiditySweepType;
    }

    public int isBosDirection() {
        return bosDirection;
    }

    public void setBosDirection(int bosDirection) {
        this.bosDirection = bosDirection;
    }

    public double getBuyLiquidity() {
        return buyLiquidity;
    }

    public void setBuyLiquidity(double buyLiquidity) {
        this.buyLiquidity = buyLiquidity;
    }

    public double getSellLiquidity() {
        return sellLiquidity;
    }

    public void setSellLiquidity(double sellLiquidity) {
        this.sellLiquidity = sellLiquidity;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public double getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public boolean isTradeResult() {
        return tradeResult;
    }

    public void setTradeResult(boolean tradeResult) {
        this.tradeResult = tradeResult;
    }
}
