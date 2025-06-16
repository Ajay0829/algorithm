package com.market.analysis.technical.model;

import com.market.common.Stock;
import com.market.common.SwingPoint;

import java.util.List;
import java.util.Objects;

public class MarketStructure {
    private Stock stock;
    private MarketTrend currentTrend;
    private List<SwingPoint> swingPoints;
    private List<BreakOfStructure> breakOfStructures;
    private List<LiquiditySweep> liquiditySweeps;

    public MarketStructure(Stock stock, MarketTrend currentTrend, List<SwingPoint> swingPoints, List<BreakOfStructure> breakOfStructures, List<LiquiditySweep> liquiditySweeps) {
        this.stock = stock;
        this.currentTrend = currentTrend;
        this.swingPoints = swingPoints;
        this.breakOfStructures = breakOfStructures;
        this.liquiditySweeps = liquiditySweeps;
    }

    public Stock getStock() {
        return stock;
    }

    public MarketTrend getCurrentTrend() {
        return currentTrend;
    }

    public List<SwingPoint> getSwingPoints() {
        return swingPoints;
    }

    public List<BreakOfStructure> getBreakOfStructures() {
        return breakOfStructures;
    }

    public List<LiquiditySweep> getLiquiditySweeps() {
        return liquiditySweeps;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public void setCurrentTrend(MarketTrend currentTrend) {
        this.currentTrend = currentTrend;
    }

    public void setSwingPoints(List<SwingPoint> swingPoints) {
        this.swingPoints = swingPoints;
    }

    public void setBreakOfStructures(List<BreakOfStructure> breakOfStructures) {
        this.breakOfStructures = breakOfStructures;
    }

    public void setLiquiditySweeps(List<LiquiditySweep> liquiditySweeps) {
        this.liquiditySweeps = liquiditySweeps;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MarketStructure that = (MarketStructure) o;
        return currentTrend == that.currentTrend && Objects.equals(swingPoints, that.swingPoints) && Objects.equals(breakOfStructures, that.breakOfStructures) && Objects.equals(liquiditySweeps, that.liquiditySweeps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTrend, swingPoints, breakOfStructures, liquiditySweeps);
    }
}
