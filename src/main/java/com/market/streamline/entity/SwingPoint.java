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

}

