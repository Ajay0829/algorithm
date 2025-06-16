package com.market.analysis.technical.service;

import com.market.analysis.technical.model.MarketStructure;
import com.market.analysis.technical.model.MarketTrend;
import com.market.analysis.technical.model.MarketStructureEvent;
import com.market.common.Stock;
import com.market.common.SwingPoint;
import com.market.external.polygon.dto.Candle;
import com.market.external.polygon.dto.PolygonAggregatesResponse;
import com.market.external.polygon.service.PolygonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketStructureService {

    private static final Logger logger = LoggerFactory.getLogger(MarketStructureService.class);

    private final PolygonService polygonService;
    private final SwingPointService swingPointService;
    private final MarketTrendService marketTrendService;
    private final MarketStructureEventService marketStructureEventService;

    public MarketStructureService(
            PolygonService polygonService,
            SwingPointService swingPointService,
            MarketTrendService marketTrendService,
            MarketStructureEventService marketStructureEventService
    ) {
        this.polygonService = polygonService;
        this.swingPointService = swingPointService;
        this.marketTrendService = marketTrendService;
        this.marketStructureEventService = marketStructureEventService;
    }

    public MarketStructure getMarketStructure(Stock stock, String startDate, String endDate) {

        // TODO: Add support for multiple timeframes
        PolygonAggregatesResponse response = polygonService.getAggregates(
                stock.getSymbol(), "1", "day", startDate, endDate
        );
        List<Candle> candles = response.getResults();
        List<SwingPoint> swingPoints = swingPointService.getSwingPoints(candles);
        List<MarketStructureEvent> marketStructureEvents = marketStructureEventService.detectMarketStructureEvents(swingPoints);
        MarketTrend currentTrend = marketTrendService.getMarketTrend(marketStructureEvents);

        return new MarketStructure(
                stock,
                candles,
                currentTrend,
                swingPoints,
                marketStructureEvents
        );
    }
}
