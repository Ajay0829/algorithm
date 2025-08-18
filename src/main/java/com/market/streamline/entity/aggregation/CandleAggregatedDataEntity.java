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

    @Column(name = "opposing_zone_distance")
    private double opposingZoneDistance;

    @Column(name = "opposing_zone_strength")
    private double opposingZoneStrength;

    @Column(name = "opposing_zone_volume")
    private double opposingZoneVolume;

    @Column(name = "same_zone_strength")
    private double sameZoneStrength;

    @Column(name = "same_zone_volume")
    private double sameZoneVolume;

    @Column(name = "liquidity_sweep_direction")
    private String liquiditySweepDirection;

    @Column(name = "same_liquidity_distance")
    private Double sameLiquidityDistance;

    @Column(name = "opposing_liquidity_distance")
    private Double opposingLiquidityDistance;

    @Column(name = "bos_direction")
    private String bosDirection;

    @Column(name = "bos_volume")
    private double bosVolume;

    @Column(name = "trade")
    private String trade;

    @Column(name = "time_to_return")
    private Long timeToReturn;

    @Column(name = "zone_taps")
    private Integer zoneTaps;

    @Column(name = "trade_result")
    private String tradeResult;

    @Column(name = "half_life")
    private Integer halfLife;

    @Column(name = "resilience")
    private Double resilience;

    @Column(name = "volatility14")
    private double volatility14;

    @Column(name = "volatility50")
    private double volatility50;

    @Column(name = "volatility200")
    private double volatility200;

    @Column(name = "volume14")
    private double volume14;

    @Column(name = "volume50")
    private double volume50;

    @Column(name = "volume200")
    private double volume200;

    @Column(name = "rsi_14")
    private double rsi14;

    @Column(name = "rsi_50")
    private double rsi50;

    @Column(name = "same_direction_max_move")
    private double sameDirectionMaxMove;

    @Column(name = "entry_price")
    private double entryPrice;

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

    public double getOpposingZoneDistance() {
        return opposingZoneDistance;
    }

    public void setOpposingZoneDistance(double opposingZoneDistance) {
        this.opposingZoneDistance = opposingZoneDistance;
    }

    public double getOpposingZoneStrength() {
        return opposingZoneStrength;
    }

    public void setOpposingZoneStrength(double opposingZoneStrength) {
        this.opposingZoneStrength = opposingZoneStrength;
    }

    public double getOpposingZoneVolume() {
        return opposingZoneVolume;
    }

    public void setOpposingZoneVolume(double opposingZoneVolume) {
        this.opposingZoneVolume = opposingZoneVolume;
    }

    public double getSameZoneStrength() {
        return sameZoneStrength;
    }

    public void setSameZoneStrength(double sameZoneStrength) {
        this.sameZoneStrength = sameZoneStrength;
    }

    public double getSameZoneVolume() {
        return sameZoneVolume;
    }

    public void setSameZoneVolume(double sameZoneVolume) {
        this.sameZoneVolume = sameZoneVolume;
    }

    public String getLiquiditySweepDirection() {
        return liquiditySweepDirection;
    }

    public void setLiquiditySweepDirection(String liquiditySweepDirection) {
        this.liquiditySweepDirection = liquiditySweepDirection;
    }

    public Double getSameLiquidityDistance() {
        return sameLiquidityDistance;
    }

    public void setSameLiquidityDistance(Double sameLiquidityDistance) {
        this.sameLiquidityDistance = sameLiquidityDistance;
    }

    public Double getOpposingLiquidityDistance() {
        return opposingLiquidityDistance;
    }

    public void setOpposingLiquidityDistance(Double opposingLiquidityDistance) {
        this.opposingLiquidityDistance = opposingLiquidityDistance;
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

    public Integer getZoneTaps() {
        return zoneTaps;
    }

    public void setZoneTaps(Integer zoneTaps) {
        this.zoneTaps = zoneTaps;
    }

    public Integer getHalfLife() {
        return halfLife;
    }

    public void setHalfLife(Integer halfLife) {
        this.halfLife = halfLife;
    }

    public Double getResilience() {
        return resilience;
    }

    public void setResilience(Double resilience) {
        this.resilience = resilience;
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


    public double getSameDirectionMaxMove() {
        return sameDirectionMaxMove;
    }

    public void setSameDirectionMaxMove(double sameDirectionMaxMove) {
        this.sameDirectionMaxMove = sameDirectionMaxMove;
    }

    public double getVolatility14() {
        return volatility14;
    }

    public void setVolatility14(double volatility14) {
        this.volatility14 = volatility14;
    }

    public double getVolatility50() {
        return volatility50;
    }

    public void setVolatility50(double volatility50) {
        this.volatility50 = volatility50;
    }

    public double getVolatility200() {
        return volatility200;
    }

    public void setVolatility200(double volatility200) {
        this.volatility200 = volatility200;
    }

    public double getVolume14() {
        return volume14;
    }

    public void setVolume14(double volume14) {
        this.volume14 = volume14;
    }

    public double getVolume50() {
        return volume50;
    }

    public void setVolume50(double volume50) {
        this.volume50 = volume50;
    }

    public double getVolume200() {
        return volume200;
    }

    public void setVolume200(double volume200) {
        this.volume200 = volume200;
    }

    public double getRsi50() {
        return rsi50;
    }

    public void setRsi50(double rsi50) {
        this.rsi50 = rsi50;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }
}
