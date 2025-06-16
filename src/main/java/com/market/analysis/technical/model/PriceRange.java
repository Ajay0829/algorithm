package com.market.analysis.technical.model;

public class PriceRange {
    private double high;
    private double low;

    public PriceRange(double high, double low) {
        this.high = high;
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    @Override
    public String toString() {
        return "PriceRange{" +
                "high=" + high +
                ", low=" + low +
                '}';
    }
}
