package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trends", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class Trend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "type") // e.g., UP, DOWN, SIDEWAYS
    private String type;

    @Column(name = "strength")
    private Double strength;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "strong_swing_point_id")
    private SwingPoint strongSwingPoint;

    public Trend() {
    }

    public Trend(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String type, Double strength, SwingPoint strongSwingPoint) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.type = type;
        this.strength = strength;
        this.strongSwingPoint = strongSwingPoint;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getStrength() {
        return strength;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }

    public SwingPoint getStrongSwingPoint() {
        return strongSwingPoint;
    }

    public void setStrongSwingPoint(SwingPoint strongSwingPoint) {
        this.strongSwingPoint = strongSwingPoint;
    }
}
