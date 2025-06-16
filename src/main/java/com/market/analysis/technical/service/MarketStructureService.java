package com.market.analysis.technical.service;

import com.market.analysis.technical.model.*;
import com.market.common.Stock;
import com.market.common.SwingPoint;
import com.market.external.polygon.dto.Candle;
import com.market.external.polygon.dto.PolygonAggregatesResponse;
import com.market.external.polygon.service.PolygonService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketStructureService {

    private final PolygonService polygonService;
    private final SwingPointService swingPointService;
    private final MarketTrendService marketTrendService;
    private final MarketStructureEventService marketStructureEventService;
    private final ZoneDetectionService zoneDetectionService;

    public MarketStructureService(
            PolygonService polygonService,
            SwingPointService swingPointService,
            MarketTrendService marketTrendService,
            MarketStructureEventService marketStructureEventService,
            ZoneDetectionService zoneDetectionService
    ) {
        this.polygonService = polygonService;
        this.swingPointService = swingPointService;
        this.marketTrendService = marketTrendService;
        this.marketStructureEventService = marketStructureEventService;
        this.zoneDetectionService = zoneDetectionService;
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
        List<DemandZone> demandZones = zoneDetectionService.detectDemandZones(candles, swingPoints, marketStructureEvents);
        List<SupplyZone> supplyZones = zoneDetectionService.detectSupplyZones(candles, swingPoints, marketStructureEvents);

        return new MarketStructure(
                stock,
                candles,
                currentTrend,
                swingPoints,
                marketStructureEvents,
                demandZones,
                supplyZones
        );
    }
}
