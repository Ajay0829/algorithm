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

    @Column(name = "average_volatility")
    private Double averageVolatility;

    @Column(name = "average_volume")
    private Double averageVolume;

    @Column(name = "rsi_14")
    private Double rsi14;

    @Column(name = "no_of_samples")
    private int noOfSamples;

    public MarketIndicators() {}

    public MarketIndicators(String stockSymbol, String timeframe, Double averageVolatility, Double averageVolume, Double rsi14, int noOfSamples) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.averageVolatility = averageVolatility;
        this.averageVolume = averageVolume;
        this.rsi14 = rsi14;
        this.noOfSamples = noOfSamples;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public Double getAverageVolatility() { return averageVolatility; }
    public void setAverageVolatility(Double averageVolatility) { this.averageVolatility = averageVolatility; }

    public Double getAverageVolume() { return averageVolume; }
    public void setAverageVolume(Double averageVolume) { this.averageVolume = averageVolume; }

    public Double getRsi14() { return rsi14; }
    public void setRsi14(Double rsi14) { this.rsi14 = rsi14; }

    // Backward compatibility methods for existing code
    public Double getVolatility() { return averageVolatility; }
    public void setVolatility(Double volatility) { this.averageVolatility = volatility; }


    public int getNoOfSamples() {
        return noOfSamples;
    }

    public void setNoOfSamples(int noOfSamples) {
        this.noOfSamples = noOfSamples;
    }
}
