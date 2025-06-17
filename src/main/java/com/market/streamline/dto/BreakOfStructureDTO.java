package com.market.streamline.dto;

import java.time.LocalDateTime;

public class BreakOfStructureDTO {
    public String stockSymbol;
    public String timeframe;
    public LocalDateTime candleTimestamp;
    public String type;
    public Long weakSwingPointId;
    public Long strongSwingPointId;
}

