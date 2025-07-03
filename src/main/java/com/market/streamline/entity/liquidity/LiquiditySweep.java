package com.market.streamline.entity.liquidity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidity_sweeps", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class LiquiditySweep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "sweep_type") // e.g., BUY, SELL
    private String sweepType;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "price")
    private Double price;

    @Column(name = "no_of_equals")
    private Integer noOfEquals;

    public LiquiditySweep() {
    }

    public LiquiditySweep(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String sweepType, Double volume, Double price, Integer noOfEquals) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.sweepType = sweepType;
        this.volume = volume;
        this.price = price;
        this.noOfEquals = noOfEquals;
    }

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

    public String getSweepType() {
        return sweepType;
    }

    public void setSweepType(String sweepType) {
        this.sweepType = sweepType;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getNoOfEquals() {
        return noOfEquals;
    }

    public void setNoOfEquals(Integer noOfEquals) {
        this.noOfEquals = noOfEquals;
    }
}

