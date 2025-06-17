package com.market.streamline.extractor;

import com.market.streamline.dto.ZoneDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ZoneExtractor {
    public ZoneDTO extract(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, double[] prices) {
        ZoneDTO dto = new ZoneDTO();
        dto.stockSymbol = stockSymbol;
        dto.timeframe = timeframe;
        dto.candleTimestamp = candleTimestamp;
        dto.nearPoint = prices[0]; // Dummy logic
        dto.farPoint = prices[1]; // Dummy logic
        dto.type = "SUPPLY"; // Dummy logic
        dto.volume = 1000.0; // Dummy value
        dto.strength = 1.0; // Dummy value
        return dto;
    }
}

