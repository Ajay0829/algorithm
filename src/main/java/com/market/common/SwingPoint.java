package com.market.common;

import com.market.external.polygon.dto.Candle;

public class SwingPoint {
    final int index;
    final double price;
    final Candle candle;
    final SwingType swingType;

    public SwingPoint(int index, double price, Candle candle, SwingType swingType) {
        this.index = index;
        this.price = price;
        this.candle = candle;
        this.swingType = swingType;
    }

    public int getIndex() {
        return index;
    }

    public double getPrice() {
        return price;
    }

    public SwingType getSwingType() {
        return swingType;
    }

    public Candle getCandle() {
        return candle;
    }

    public boolean isHigh() {
        return swingType == SwingType.HIGH;
    }

    public boolean isLow() {
        return swingType == SwingType.LOW;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwingPoint{");
        sb.append("index=").append(index);
        sb.append(", price=").append(price);
        sb.append(", swingType=").append(swingType);
        sb.append(", candle=").append(candle);
        sb.append('}');
        return sb.toString();
    }
}