package com.market.streamline.extractor;

import com.market.streamline.dto.LiquiditySweepDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class LiquiditySweepExtractor {
    public LiquiditySweepDTO extract(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, double[] prices) {
        LiquiditySweepDTO dto = new LiquiditySweepDTO();
        dto.stockSymbol = stockSymbol;
        dto.timeframe = timeframe;
        dto.candleTimestamp = candleTimestamp;
        dto.type = "BUY"; // Dummy logic
        dto.price = prices[0]; // Dummy logic
        dto.noOfEquals = 5; // Dummy value
        return dto;
    }
}

