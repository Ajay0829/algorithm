package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.model.CandleEvent;
import com.market.streamline.plot.GenericPlotExporter;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.SwingPointRepository;
import com.market.streamline.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CandleEventConsumer {

    @Autowired
    private FeatureExtractionService featureExtractionService;

    @Autowired
    private SwingPointService swingPointService;

    @Autowired
    private GenericPlotExporter genericPlotExporter;

    @Autowired
    private CandleRepository candleRepository;

    @Autowired
    private SwingPointRepository swingPointRepository;

    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private int eventCount = 0;
    private int totalEvents = 0;
    private String stockSymbol = null;
    @Autowired
    private BreakOfStructureService breakOfStructureService;
    @Autowired
    private LiquidityService liquidityService;
    @Autowired
    private ZoneService zoneService;

    public void setTotalEvents(int total, String symbol) {
        this.totalEvents = total;
        this.stockSymbol = symbol;
    }

    @KafkaListener(topics = "candle-added", groupId = "feature-extractor-group")
    public void listenCandleAdded(String message) {
        try {
            CandleEvent candleEvent = objectMapper.readValue(message, CandleEvent.class);

            LocalDateTime candleTime = LocalDateTime.parse(candleEvent.getCandleTimestamp(), DateTimeFormatter.ISO_DATE_TIME);
            CandleEntity candleEntity = new CandleEntity(
                    candleEvent.getStockSymbol(),
                    candleEvent.getTimeframe(),
                    candleTime,
                    candleEvent.getOpen(),
                    candleEvent.getClose(),
                    candleEvent.getHigh(),
                    candleEvent.getLow(),
                    candleEvent.getVolume()
            );
            candleRepository.save(candleEntity);
            breakOfStructureService.checkForBreakOfStructure(candleEntity);
            swingPointService.checkForSwingPoint(candleEntity);
            liquidityService.invalidateLiquidityZones(candleEntity);
            zoneService.invalidateZones(candleEntity);
            zoneService.updateZoneStrength(candleEntity);
            eventCount++;
            // After all events are processed, export for plotting
            if (eventCount == totalEvents && stockSymbol != null) {
                genericPlotExporter.exportPlotCsv(stockSymbol, candleEvent.getTimeframe(), stockSymbol + "_swing_points.csv");
                System.out.println("Exported plot points for " + stockSymbol);
                // Reset the repositories
                candleRepository.deleteAllInBatch();
                swingPointRepository.deleteAllInBatch();
                breakOfStructureRepository.deleteAllInBatch();
                eventCount = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

