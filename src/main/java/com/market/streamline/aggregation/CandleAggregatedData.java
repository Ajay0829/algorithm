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

    // Swing points
    private double lastSwingHigh;
    private double lastSwingLow;

    // Supply and demand data
    private double supplyPrice;
    private double supplyVolume;
    private double demandPrice;
    private double demandVolume;

    // Break of structure (BoS) data
    private String bosDirection;
    private double bosVolume;

    // Liquidity Sweep data
    private String lastLiquiditySweepType;

    // Liquidity data
    private double buyLiquidity;
    private int buyLiquidityStrength;
    private double sellLiquidity;
    private int sellLiquidityStrength;

    // Indicators data
    private double volatility;
    private double averageVolume;
    private double rsi14;

    // Trade data
    private String trade;
    private double entryPrice;
    private String tradeResult;

    public CandleAggregatedData() {

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

    public double getSupplyVolume() {
        return supplyVolume;
    }

    public void setSupplyVolume(double supplyVolume) {
        this.supplyVolume = supplyVolume;
    }

    public double getDemandPrice() {
        return demandPrice;
    }

    public void setDemandPrice(double demandPrice) {
        this.demandPrice = demandPrice;
    }

    public double getDemandVolume() {
        return demandVolume;
    }

    public void setDemandVolume(double demandVolume) {
        this.demandVolume = demandVolume;
    }

    public String getBosDirection() {
        return bosDirection;
    }

    public void setBosDirection(String bosDirection) {
        this.bosDirection = bosDirection;
    }

    public double getBosVolume() {
        return bosVolume;
    }

    public void setBosVolume(double bosVolume) {
        this.bosVolume = bosVolume;
    }

    public String getLastLiquiditySweepType() {
        return lastLiquiditySweepType;
    }

    public void setLastLiquiditySweepType(String lastLiquiditySweepType) {
        this.lastLiquiditySweepType = lastLiquiditySweepType;
    }

    public double getBuyLiquidity() {
        return buyLiquidity;
    }

    public void setBuyLiquidity(double buyLiquidity) {
        this.buyLiquidity = buyLiquidity;
    }

    public int getBuyLiquidityStrength() {
        return buyLiquidityStrength;
    }

    public void setBuyLiquidityStrength(int buyLiquidityStrength) {
        this.buyLiquidityStrength = buyLiquidityStrength;
    }

    public double getSellLiquidity() {
        return sellLiquidity;
    }

    public void setSellLiquidity(double sellLiquidity) {
        this.sellLiquidity = sellLiquidity;
    }

    public int getSellLiquidityStrength() {
        return sellLiquidityStrength;
    }

    public void setSellLiquidityStrength(int sellLiquidityStrength) {
        this.sellLiquidityStrength = sellLiquidityStrength;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public double getAverageVolume() {
        return averageVolume;
    }

    public void setAverageVolume(double averageVolume) {
        this.averageVolume = averageVolume;
    }

    public double getRsi14() {
        return rsi14;
    }

    public void setRsi14(double rsi14) {
        this.rsi14 = rsi14;
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

    public String getTradeResult() {
        return tradeResult;
    }

    public void setTradeResult(String tradeResult) {
        this.tradeResult = tradeResult;
    }
}
