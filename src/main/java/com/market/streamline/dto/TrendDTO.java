package com.market.streamline.dto;

import java.time.LocalDateTime;

public class TrendDTO {
    public String stockSymbol;
    public String timeframe;
    public LocalDateTime candleTimestamp;
    public String type;
    public Double strength;
}

