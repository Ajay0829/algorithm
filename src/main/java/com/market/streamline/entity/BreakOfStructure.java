package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class BreakOfStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "direction") // e.g., UP, DOWN
    private String direction;

    @Column(name = "type")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weak_swing_point")
    private SwingPoint weakSwingPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strong_swing_point")
    private SwingPoint strongSwingPoint;
}

