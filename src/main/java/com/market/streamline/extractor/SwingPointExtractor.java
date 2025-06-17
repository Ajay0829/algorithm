package com.market.streamline.extractor;

import com.market.streamline.dto.SwingPointDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SwingPointExtractor {
    public SwingPointDTO extract(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, double[] prices) {
        // Dummy logic: pick max/min as swing high/low
        SwingPointDTO dto = new SwingPointDTO();
        dto.stockSymbol = stockSymbol;
        dto.timeframe = timeframe;
        dto.candleTimestamp = candleTimestamp;
        dto.price = prices[0]; // Replace with actual swing logic
        dto.swingType = "HIGH"; // or "LOW" based on logic
        return dto;
    }
}

