package com.market.streamline.dto;

public class ChartBOSDTO {
    private String type; // "bos"
    private String action; // "created", "updated", "deleted"
    private BOSData data;

    public ChartBOSDTO() {}

    public ChartBOSDTO(String type, String action, BOSData data) {
        this.type = type;
        this.action = action;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public BOSData getData() { return data; }
    public void setData(BOSData data) { this.data = data; }

    public static class BOSData {
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
}
