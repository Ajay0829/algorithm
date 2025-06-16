package com.market.analysis.technical.service;

import com.market.analysis.technical.model.BreakOfStructure;
import com.market.analysis.technical.model.LiquiditySweep;
import com.market.analysis.technical.model.MarketStructure;
import com.market.analysis.technical.model.MarketTrend;
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
    private final BreakOfStructureService breakOfStructureService;
    private final LiquiditySweepService liquiditySweepService;

    public MarketStructureService(
            PolygonService polygonService,
            SwingPointService swingPointService,
            MarketTrendService marketTrendService,
            BreakOfStructureService breakOfStructureService,
            LiquiditySweepService liquiditySweepService
    ) {
        this.polygonService = polygonService;
        this.swingPointService = swingPointService;
        this.marketTrendService = marketTrendService;
        this.breakOfStructureService = breakOfStructureService;
        this.liquiditySweepService = liquiditySweepService;
    }

    public MarketStructure getMarketStructure(Stock stock, String startDate, String endDate) {

        // TODO: Add support for multiple timeframes
        PolygonAggregatesResponse response = polygonService.getAggregates(
                stock.getSymbol(), "1", "day", startDate, endDate
        );
        List<Candle> candles = response.getResults();
        List<SwingPoint> swingPoints = swingPointService.getSwingPoints(candles);
        MarketTrend currentTrend = marketTrendService.getMarketTrend(swingPoints);
        List<BreakOfStructure> breakOfStructures = breakOfStructureService.getBreakOfStructures(swingPoints);
        List<LiquiditySweep> liquiditySweeps = liquiditySweepService.getLiquiditySweeps(swingPoints);

        return new MarketStructure(
                stock,
                currentTrend,
                swingPoints,
                breakOfStructures,
                liquiditySweeps
        );
    }
}
