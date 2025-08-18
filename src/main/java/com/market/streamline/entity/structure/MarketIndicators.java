package com.market.streamline.entity.structure;

import jakarta.persistence.*;

@Entity
@Table(name = "market_indicators")
public class MarketIndicators {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "volatility14")
    private Double volatility14;

    @Column(name = "volatility50")
    private Double volatility50;

    @Column(name = "volatility200")
    private Double volatility200;

    @Column(name = "volume14")
    private Double volume14;

    @Column(name = "volume50")
    private Double volume50;

    @Column(name = "volume200")
    private Double volume200;

    @Column(name = "rsi_14")
    private Double rsi14;

    @Column(name = "rsi_50")
    private Double rsi50;

    @Column(name = "no_of_samples")
    private int noOfSamples;

    public MarketIndicators() {}

    public MarketIndicators(String stockSymbol, String timeframe, Double volatility14, Double volatility50, Double volatility200, Double volume14, Double volume50, Double volume200, Double rsi14, Double rsi50, int noOfSamples) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.volatility14 = volatility14;
        this.volatility50 = volatility50;
        this.volatility200 = volatility200;
        this.volume14 = volume14;
        this.volume50 = volume50;
        this.volume200 = volume200;
        this.rsi14 = rsi14;
        this.rsi50 = rsi50;
        this.noOfSamples = noOfSamples;
    }


    public int getNoOfSamples() {
        return noOfSamples;
    }

    public void setNoOfSamples(int noOfSamples) {
        this.noOfSamples = noOfSamples;
    }

    public Double getRsi50() {
        return rsi50;
    }

    public void setRsi50(Double rsi50) {
        this.rsi50 = rsi50;
    }

    public Double getRsi14() {
        return rsi14;
    }

    public void setRsi14(Double rsi14) {
        this.rsi14 = rsi14;
    }

    public Double getVolume200() {
        return volume200;
    }

    public void setVolume200(Double volume200) {
        this.volume200 = volume200;
    }

    public Double getVolume50() {
        return volume50;
    }

    public void setVolume50(Double volume50) {
        this.volume50 = volume50;
    }

    public Double getVolume14() {
        return volume14;
    }

    public void setVolume14(Double volume14) {
        this.volume14 = volume14;
    }

    public Double getVolatility200() {
        return volatility200;
    }

    public void setVolatility200(Double volatility200) {
        this.volatility200 = volatility200;
    }

    public Double getVolatility50() {
        return volatility50;
    }

    public void setVolatility50(Double volatility50) {
        this.volatility50 = volatility50;
    }

    public Double getVolatility14() {
        return volatility14;
    }

    public void setVolatility14(Double volatility14) {
        this.volatility14 = volatility14;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}
