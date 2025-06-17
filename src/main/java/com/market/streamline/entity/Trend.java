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

    @Column(name = "type") // e.g., UP, DOWN, SIDEWAYS
    private String type;

    @Column(name = "strength")
    private Double strength;
}

