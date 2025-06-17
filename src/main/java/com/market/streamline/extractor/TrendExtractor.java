package com.market.streamline.extractor;

import com.market.streamline.dto.TrendDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class TrendExtractor {
    public TrendDTO extract(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, double[] prices) {
        TrendDTO dto = new TrendDTO();
        dto.stockSymbol = stockSymbol;
        dto.timeframe = timeframe;
        dto.candleTimestamp = candleTimestamp;
        dto.type = "UP"; // Dummy logic, replace with actual trend logic
        dto.strength = 1.0; // Dummy value
        return dto;
    }
}

