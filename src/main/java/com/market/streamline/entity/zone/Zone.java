package com.market.streamline.entity.zone;

import com.market.streamline.entity.structure.SwingPoint;
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

    @Column(name = "no_of_taps")
    private Integer noOfTaps;

    @Column(name = "risk_per_unit")
    private Double riskPerUnit;

    @Column(name = "half_life")
    private Integer halfLife;

    @Column(name = "resilience")
    private Double resilience;

    @Column(name = "same_direction_move")
    private double sameDirectionExtremeMovement;

    @Column(name = "impulse_extending")
    private Boolean impulseExtending;

    @Column(name = "identified_at")
    private LocalDateTime identifiedAt;

    public Zone(String stockSymbol,
                String timeframe,
                LocalDateTime candleTimestamp,
                String zoneType,
                Double nearPoint,
                Double farPoint,
                String type,
                Double volume,
                Double strength,
                Integer noOfTaps,
                LocalDateTime identifiedAt,
                Double riskPerUnit,
                Integer halfLife,
                Double resilience,
                Double sameDirectionExtremeMovement,
                Boolean impulseExtending) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.candleTimestamp = candleTimestamp;
        this.zoneType = zoneType;
        this.nearPoint = nearPoint;
        this.farPoint = farPoint;
        this.type = type;
        this.volume = volume;
        this.strength = strength;
        this.noOfTaps = noOfTaps;
        this.identifiedAt = identifiedAt;
        this.riskPerUnit = riskPerUnit;
        this.halfLife = halfLife;
        this.resilience = resilience;
        this.sameDirectionExtremeMovement = sameDirectionExtremeMovement;
        this.impulseExtending = impulseExtending;
    }

    public Zone() {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCandleTimestamp() {
        return candleTimestamp;
    }

    public void setCandleTimestamp(LocalDateTime candleTimestamp) {
        this.candleTimestamp = candleTimestamp;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
    }

    public Double getNearPoint() {
        return nearPoint;
    }

    public void setNearPoint(Double nearPoint) {
        this.nearPoint = nearPoint;
    }

    public Double getFarPoint() {
        return farPoint;
    }

    public void setFarPoint(Double farPoint) {
        this.farPoint = farPoint;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getStrength() {
        return strength;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }

    public Integer getNoOfTaps() {
        return noOfTaps;
    }

    public void setNoOfTaps(Integer noOfTaps) {
        this.noOfTaps = noOfTaps;
    }

    public LocalDateTime getIdentifiedAt() {
        return identifiedAt;
    }

    public void setIdentifiedAt(LocalDateTime identifiedAt) {
        this.identifiedAt = identifiedAt;
    }

    public Double getRiskPerUnit() {
        return riskPerUnit;
    }

    public void setRiskPerUnit(Double riskPerUnit) {
        this.riskPerUnit = riskPerUnit;
    }

    public Integer getHalfLife() {
        return halfLife;
    }

    public void setHalfLife(Integer halfLife) {
        this.halfLife = halfLife;
    }

    public Double getResilience() {
        return resilience;
    }

    public void setResilience(Double resilience) {
        this.resilience = resilience;
    }

    public Boolean getImpulseExtending() {
        return impulseExtending;
    }

    public void setImpulseExtending(Boolean impulseExtending) {
        this.impulseExtending = impulseExtending;
    }

    public double getSameDirectionExtremeMovement() {
        return sameDirectionExtremeMovement;
    }

    public void setSameDirectionExtremeMovement(double sameDirectionExtremeMovement) {
        this.sameDirectionExtremeMovement = sameDirectionExtremeMovement;
    }
}


