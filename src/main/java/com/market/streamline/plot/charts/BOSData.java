package com.market.streamline.plot.charts;

public class BOSData {
    private String start;     // Start timestamp
    private String end;       // End timestamp
    private String bosType;   // "BULLISH" or "BEARISH"
    private String timeframe; // "1d", "1h", etc.
    private double y;         // Price level

    public BOSData() {}

    public BOSData(String start, String end, String bosType, String timeframe, double y) {
        this.start = start;
        this.end = end;
        this.bosType = bosType;
        this.timeframe = timeframe;
        this.y = y;
    }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }
    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }
    public String getBosType() { return bosType; }
    public void setBosType(String bosType) { this.bosType = bosType; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}
