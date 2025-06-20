package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "swing_points", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class SwingPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "swing_type") // HIGH or LOW
    private String swingType;

    @Column(name = "price")
    private Double price;

    @Column
    private Boolean confirmed = false;

    public SwingPoint(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String swingType, Double price, Boolean confirmed) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.swingType = swingType;
        this.price = price;
        this.confirmed = confirmed;
    }

    public SwingPoint() {

    }

    public Long getId() {
        return id;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public LocalDateTime getCandleTimestamp() {
        return candleTimestamp;
    }

    public String getSwingType() {
        return swingType;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getConfirmed() { return confirmed;}

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}

