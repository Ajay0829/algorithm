package com.market.streamline.dto.charts;

public class SwingData {
    private String swingType; // "major_high", "major_low", "minor_high", "minor_low"
    private String timestamp; // Candle timestamp
    private boolean useHigh; // true for high, false for low
    private String timeframe; // e.g. "1d"

    public SwingData() {}

    public SwingData(String swingType, String timestamp, boolean useHigh, String timeframe) {
        this.swingType = swingType;
        this.timestamp = timestamp;
        this.useHigh = useHigh;
        this.timeframe = timeframe;
    }

    public String getSwingType() { return swingType; }
    public void setSwingType(String swingType) { this.swingType = swingType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public boolean isUseHigh() { return useHigh; }
    public void setUseHigh(boolean useHigh) { this.useHigh = useHigh; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
}
