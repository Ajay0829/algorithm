package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "zones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp", "zone_type"})
})
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "zone_type") // e.g., SUPPLY, DEMAND
    private String zoneType;

    @Column(name = "near_point")
    private Double nearPoint;

    @Column(name = "far_point")
    private Double farPoint;

    @Column(name = "type")
    private String type;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "strength")
    private Double strength;
}

