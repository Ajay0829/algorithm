package com.market.common;

public class SwingPoint {
    final int index;
    final double price;
    final SwingType swingType;

    public SwingPoint(int index, double price, SwingType swingType) {
        this.index = index;
        this.price = price;
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
        sb.append('}');
        return sb.toString();
    }
}