package com.market.streamline.entity.structure;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp", "type"})
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

    @Column(name = "bos_volume")
    private Double bosVolume;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "weak_swing_point",
        foreignKey = @ForeignKey(
            name = "bos_weak_swing_point_fkey",
            foreignKeyDefinition = "FOREIGN KEY (weak_swing_point) REFERENCES swing_points(id) ON DELETE CASCADE"
        )
    )
    private SwingPoint weakSwingPoint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "strong_swing_point",
        foreignKey = @ForeignKey(
            name = "bos_strong_swing_point_fkey",
            foreignKeyDefinition = "FOREIGN KEY (strong_swing_point) REFERENCES swing_points(id) ON DELETE CASCADE"
        )
    )
    private SwingPoint strongSwingPoint;

    public BreakOfStructure(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, String direction, String type, SwingPoint weakSwingPoint, SwingPoint strongSwingPoint, Double bosVolume) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.direction = direction;
        this.type = type;
        this.weakSwingPoint = weakSwingPoint;
        this.strongSwingPoint = strongSwingPoint;
        this.bosVolume = bosVolume;
    }

    public BreakOfStructure() {

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

    public String getDirection() {
        return direction;
    }

    public String getType() {
        return type;
    }

    public Double getBosVolume() {
        return bosVolume;
    }

    public void setBosVolume(Double bosVolume) {
        this.bosVolume = bosVolume;
    }

    public SwingPoint getWeakSwingPoint() {
        return weakSwingPoint;
    }

    public SwingPoint getStrongSwingPoint() {
        return strongSwingPoint;
    }
}
