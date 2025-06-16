package com.market.analysis.technical.model;

import com.market.common.Stock;
import com.market.common.SwingPoint;
import com.market.external.polygon.dto.Candle;

import java.util.List;
import java.util.Objects;

public class MarketStructure {
    private Stock stock;
    private List<Candle> candles;
    private MarketTrend currentTrend;
    private List<SwingPoint> swingPoints;
    private List<MarketStructureEvent> marketStructureEvents;
    private List<DemandZone> demandZones;
    private List<SupplyZone> supplyZones;

    public MarketStructure(
            Stock stock,
            List<Candle> candles,
            MarketTrend currentTrend,
            List<SwingPoint> swingPoints,
            List<MarketStructureEvent> marketStructureEvents,
            List<DemandZone> demandZones,
            List<SupplyZone> supplyZones
    ) {
        this.stock = stock;
        this.candles = candles;
        this.currentTrend = currentTrend;
        this.swingPoints = swingPoints;
        this.marketStructureEvents = marketStructureEvents;
        this.demandZones = demandZones;
        this.supplyZones = supplyZones;
    }

    public Stock getStock() {
        return stock;
    }

    public List<Candle> getCandles() {
        return candles;
    }

    public MarketTrend getCurrentTrend() {
        return currentTrend;
    }

    public List<SwingPoint> getSwingPoints() {
        return swingPoints;
    }

    public List<MarketStructureEvent> getMarketStructureEvents() {
        return marketStructureEvents;
    }

    public List<DemandZone> getDemandZones() {
        return demandZones;
    }

    public List<SupplyZone> getSupplyZones() {
        return supplyZones;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public void setCandles(List<Candle> candles) {
        this.candles = candles;
    }

    public void setCurrentTrend(MarketTrend currentTrend) {
        this.currentTrend = currentTrend;
    }

    public void setSwingPoints(List<SwingPoint> swingPoints) {
        this.swingPoints = swingPoints;
    }

    public void setMarketStructureEvents(List<MarketStructureEvent> marketStructureEvents) {
        this.marketStructureEvents = marketStructureEvents;
    }

    public void setDemandZones(List<DemandZone> demandZones) {
        this.demandZones = demandZones;
    }

    public void setSupplyZones(List<SupplyZone> supplyZones) {
        this.supplyZones = supplyZones;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MarketStructure that = (MarketStructure) o;
        return currentTrend == that.currentTrend
                && Objects.equals(swingPoints, that.swingPoints)
                && Objects.equals(marketStructureEvents, that.marketStructureEvents)
                && Objects.equals(demandZones, that.demandZones)
                && Objects.equals(supplyZones, that.supplyZones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTrend, swingPoints, marketStructureEvents, demandZones, supplyZones);
    }
}
