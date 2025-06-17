package com.market.streamline.extractor;

import com.market.streamline.dto.FundamentalDTO;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class FundamentalExtractor {
    public FundamentalDTO extract(String stockSymbol, Double peForward, Double atr, String earningsReleaseSession, LocalDateTime nextEarningsDate) {
        FundamentalDTO dto = new FundamentalDTO();
        dto.stockSymbol = stockSymbol;
        dto.peForward = peForward;
        dto.atr = atr;
        dto.earningsReleaseSession = earningsReleaseSession;
        dto.nextEarningsDate = nextEarningsDate;
        return dto;
    }
}

