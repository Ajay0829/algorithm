package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_features", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class StockFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "candle_timestamp", nullable = false)
    private LocalDateTime candleTimestamp;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "open")
    private Double open;

    @Column(name = "close")
    private Double close;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "current_swing_high")
    private Double currentSwingHigh;

    @Column(name = "current_swing_low")
    private Double currentSwingLow;

    @Column(name = "current_price_relative_value")
    private Double currentPriceRelativeValue;

    @Column(name = "current_candle_distance_from_latest_swing")
    private Double currentCandleDistanceFromLatestSwing;

    @Column(name = "prev_n_candle_high")
    private Double prevNCandleHigh;

    @Column(name = "prev_n_candle_low")
    private Double prevNCandleLow;

    @Column(name = "prev_n_candle_volume")
    private Double prevNCandleVolume;

    @Column(name = "nearest_relevant_zone_near_point")
    private Double nearestRelevantZoneNearPoint;

    @Column(name = "nearest_relevant_zone_far_point")
    private Double nearestRelevantZoneFarPoint;

    @Column(name = "nearest_relevant_zone_type")
    private String nearestRelevantZoneType;

    @Column(name = "nearest_relevant_zone_strength")
    private Double nearestRelevantZoneStrength;

    @Column(name = "nearest_relevant_zone_bos_volume")
    private Double nearestRelevantZoneBosVolume;

    @Column(name = "nearby_sell_liquidity_zone")
    private Double nearbySellLiquidityZone;

    @Column(name = "nearby_buy_liquidity_zone")
    private Double nearbyBuyLiquidityZone;

    @Column(name = "rsi_14")
    private Double rsi14;

    @Column(name = "ma_14")
    private Double ma14;

    @Column(name = "is_earnings_day")
    private Boolean isEarningsDay;

    @Column(name = "earnings_release_session")
    private String earningsReleaseSession;

    @Column(name = "forward_pe")
    private Double forwardPe;

    @Column(name = "atr")
    private Double atr;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

