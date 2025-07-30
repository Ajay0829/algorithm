package com.market.streamline.entity.aggregation;

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

    @Column(name = "supply_volume")
    private double supplyVolume;

    @Column(name = "demand_price")
    private double demandPrice;

    @Column(name = "demand_volume")
    private double demandVolume;

    @Column(name = "last_liquidity_sweep_type")
    private String lastLiquiditySweepType;

    @Column(name = "bos_direction")
    private String bosDirection;

    @Column(name = "bos_volume")
    private double bosVolume;

    @Column(name = "buy_liquidity")
    private double buyLiquidity;

    @Column(name = "buy_liquidity_strength")
    private int buyLiquidityStrength;

    @Column(name = "sell_liquidity")
    private double sellLiquidity;

    @Column(name = "sell_liquidity_strength")
    private int sellLiquidityStrength;

    @Column(name = "volatility")
    private double volatility;

    @Column(name = "average_volume")
    private  double averageVolume;

    @Column(name = "rsi_14")
    private  double rsi14;

    @Column(name = "trade")
    private String trade;

    @Column(name = "entry_price")
    private double entryPrice;

    @Column(name = "trade_result")
    private String tradeResult;

    @Column(name = "time_to_return")
    private Long timeToReturn;

    @Column(name = "supply_impulse_length")
    private Long supplyImpulseLength;

    @Column(name = "demand_impulse_length")
    private Long demandImpulseLength;

    public CandleAggregatedDataEntity() {}

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

    public String getLastLiquiditySweepType() {
        return lastLiquiditySweepType;
    }

    public void setLastLiquiditySweepType(String lastLiquiditySweepType) {
        this.lastLiquiditySweepType = lastLiquiditySweepType;
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

    public Long getTimeToReturn() {
        return timeToReturn;
    }

    public void setTimeToReturn(Long timeToReturn) {
        this.timeToReturn = timeToReturn;
    }

    public Long getSupplyImpulseLength() {
        return supplyImpulseLength;
    }

    public void setSupplyImpulseLength(Long supplyImpulseLength) {
        this.supplyImpulseLength = supplyImpulseLength;
    }

    public Long getDemandImpulseLength() {
        return demandImpulseLength;
    }

    public void setDemandImpulseLength(Long demandImpulseLength) {
        this.demandImpulseLength = demandImpulseLength;
    }
}
