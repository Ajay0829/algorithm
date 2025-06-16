package com.market.analysis.technical.service;

import com.market.analysis.technical.model.MarketStructureEvent;
import com.market.analysis.technical.model.MarketTrend;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketTrendService {

    public MarketTrend getMarketTrend(List<MarketStructureEvent> marketStructureEvents) {
        if (marketStructureEvents == null || marketStructureEvents.isEmpty()) {
            return MarketTrend.CONSOLIDATION;
        }
        // Find the most recent BOS event and use its trend
        for (int i = marketStructureEvents.size() - 1; i >= 0; i--) {
            MarketStructureEvent event = marketStructureEvents.get(i);
            if (event.getEventType() == MarketStructureEvent.EventType.BREAK_OF_STRUCTURE) {
                return event.getMarketTrend();
            }
        }
        return MarketTrend.CONSOLIDATION;
    }
}
