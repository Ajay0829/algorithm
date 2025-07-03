package com.market.streamline.entity.structure;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class CandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "open")
    private Double open;

    @Column(name = "close")
    private Double close;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "volume")
    private Double volume;

    public CandleEntity() {}

    public CandleEntity(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, Double open, Double close, Double high, Double low, Double volume) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    // Getters
    public Long getId() { return id; }
    public String getStockSymbol() { return stockSymbol; }
    public String getTimeframe() { return timeframe; }
    public LocalDateTime getCandleTimestamp() { return candleTimestamp; }
    public Double getOpen() { return open; }
    public Double getClose() { return close; }
    public Double getHigh() { return high; }
    public Double getLow() { return low; }
    public Double getVolume() { return volume; }
}

