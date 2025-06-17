package com.market.streamline.entity;

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
}

