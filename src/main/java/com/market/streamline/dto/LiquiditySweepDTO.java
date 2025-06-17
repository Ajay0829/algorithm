package com.market.streamline.dto;

import java.time.LocalDateTime;

public class LiquiditySweepDTO {
    public String stockSymbol;
    public String timeframe;
    public LocalDateTime candleTimestamp;
    public String type;
    public Double price;
    public Integer noOfEquals;
}

