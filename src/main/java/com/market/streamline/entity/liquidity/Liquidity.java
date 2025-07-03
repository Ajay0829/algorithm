package com.market.streamline.entity.liquidity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp", "liquidity_type"})
})
public class Liquidity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "liquidity_type", nullable = false)
    private String liquidityType;

    @Column(name = "price")
    private Double price;

    @Column(name = "strength")
    private Integer strength;

    // Default constructor
    public Liquidity() {
    }

    // Constructor with all fields
    public Liquidity(String stockSymbol, String timeframe, LocalDateTime candleTimestamp,
                    String liquidityType, Double price, Integer strength) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.liquidityType = liquidityType;
        this.price = price;
        this.strength = strength;
    }

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

    public String getLiquidityType() {
        return liquidityType;
    }

    public void setLiquidityType(String liquidityType) {
        this.liquidityType = liquidityType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {
        return "Liquidity{" +
                "id=" + id +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", timeframe='" + timeframe + '\'' +
                ", candleTimestamp=" + candleTimestamp +
                ", liquidityType='" + liquidityType + '\'' +
                ", price=" + price +
                ", strength=" + strength +
                '}';
    }
}
