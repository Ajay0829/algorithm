package com.market.streamline.dto;

public class ChartZoneDTO {
    private String type; // "zone"
    private String action; // "created", "updated", "deleted"
    private ZoneData data;

    public ChartZoneDTO() {}

    public ChartZoneDTO(String type, String action, ZoneData data) {
        this.type = type;
        this.action = action;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public ZoneData getData() { return data; }
    public void setData(ZoneData data) { this.data = data; }

    public static class ZoneData {
        private String zoneType; // "supply" or "demand"
        private double y0;
        private double y1;
        private String startTs; // Candle timestamp
        private String endTs;   // Candle timestamp
        private String timeframe; // "1d", "1h", etc.

        public ZoneData() {}

        public ZoneData(String zoneType, double y0, double y1, String startTs, String endTs) {
            this.zoneType = zoneType;
            this.y0 = y0;
            this.y1 = y1;
            this.startTs = startTs;
            this.endTs = endTs;
        }

        public ZoneData(String zoneType, double y0, double y1, String startTs, String endTs, String timeframe) {
            this.zoneType = zoneType;
            this.y0 = y0;
            this.y1 = y1;
            this.startTs = startTs;
            this.endTs = endTs;
            this.timeframe = timeframe;
        }

        public String getZoneType() { return zoneType; }
        public void setZoneType(String zoneType) { this.zoneType = zoneType; }
        public double getY0() { return y0; }
        public void setY0(double y0) { this.y0 = y0; }
        public double getY1() { return y1; }
        public void setY1(double y1) { this.y1 = y1; }
        public String getStartTs() { return startTs; }
        public void setStartTs(String startTs) { this.startTs = startTs; }
        public String getEndTs() { return endTs; }
        public void setEndTs(String endTs) { this.endTs = endTs; }
        public String getTimeframe() { return timeframe; }
        public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    }
}
