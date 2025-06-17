package com.market.streamline.extractor;

import com.market.streamline.dto.BreakOfStructureDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class BreakOfStructureExtractor {
    public BreakOfStructureDTO extract(String stockSymbol, String timeframe, LocalDateTime candleTimestamp, Long weakSwingPointId, Long strongSwingPointId) {
        BreakOfStructureDTO dto = new BreakOfStructureDTO();
        dto.stockSymbol = stockSymbol;
        dto.timeframe = timeframe;
        dto.candleTimestamp = candleTimestamp;
        dto.type = "UP"; // Dummy logic
        dto.weakSwingPointId = weakSwingPointId;
        dto.strongSwingPointId = strongSwingPointId;
        return dto;
    }
}

