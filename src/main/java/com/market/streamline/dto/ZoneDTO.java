package com.market.streamline.dto;

import java.time.LocalDateTime;

public class ZoneDTO {
    public String stockSymbol;
    public String timeframe;
    public LocalDateTime candleTimestamp;
    public Double nearPoint;
    public Double farPoint;
    public String type;
    public Double volume;
    public Double strength;
}

