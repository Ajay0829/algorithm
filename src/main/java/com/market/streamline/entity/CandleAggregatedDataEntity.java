package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candle_aggregated_data")
public class CandleAggregatedDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol")
    private String stockSymbol;

    @Column(name = "timeframe")
    private String timeframe;

    @Column(name = "candle_timestamp")
    private LocalDateTime candleTimestamp;

    @Column(name = "open_price")
    private Double open;

    @Column(name = "close_price")
    private Double close;

    @Column(name = "high_price")
    private Double high;

    @Column(name = "low_price")
    private Double low;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "last_swing_high")
    private double lastSwingHigh;

    @Column(name = "last_swing_low")
    private double lastSwingLow;

    @Column(name = "supply_price")
    private double supplyPrice;

    @Column(name = "demand_price")
    private double demandPrice;

    @Column(name = "last_liquidity_sweep_type")
    private int lastLiquiditySweepType;

    @Column(name = "bos_direction")
    private int bosDirection;

    @Column(name = "buy_liquidity")
    private double buyLiquidity;

    @Column(name = "sell_liquidity")
    private double sellLiquidity;

    @Column(name = "volatility")
    private double volatility;

    @Column(name = "trade")
    private String trade;

    @Column(name = "entry_price")
    private double entryPrice;

    @Column(name = "target_price")
    private double targetPrice;

    @Column(name = "stop_loss_price")
    private double stopLossPrice;

    @Column(name = "trade_result")
    private boolean tradeResult;

    // Default constructor
    public CandleAggregatedDataEntity() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public double getDemandPrice() {
        return demandPrice;
    }

    public void setDemandPrice(double demandPrice) {
        this.demandPrice = demandPrice;
    }

    public int getLastLiquiditySweepType() {
        return lastLiquiditySweepType;
    }

    public void setLastLiquiditySweepType(int lastLiquiditySweepType) {
        this.lastLiquiditySweepType = lastLiquiditySweepType;
    }

    public int getBosDirection() {
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
