package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.CandleAggregatedDataEntity;
import com.market.streamline.model.CandleEvent;
import com.market.streamline.plot.GenericPlotExporter;
import com.market.streamline.repository.*;
import com.market.streamline.service.*;
import com.market.streamline.util.CandleAggregatedDataCsvExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

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
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private TrendService trendService;
    @Autowired
    private TrendRepository trendRepository;
    @Autowired
    private TradeDetectorService tradeDetectorService;
    @Autowired
    private VolatilityRepository volatilityRepository;
    @Autowired
    private VolatilityCalculationService volatilityCalculationService;
    @Autowired
    private ImpulseZoneService impulseZoneService;
    @Autowired
    private ChartAnnotationProducer chartAnnotationProducer;
    @Autowired
    private TradeSimulationService tradeSimulationService;
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private LiquidityRepository liquidityRepository;
    @Autowired
    private LiquiditySweepRepository liquiditySweepRepository;
    @Autowired
    private CandleDataAggregationService candleDataAggregationService;
    @Autowired
    private CandleAggregatedDataRepository candleAggregatedDataRepository;
    @Autowired
    private CandleAggregatedDataMapper candleAggregatedDataMapper;
    @Autowired
    private CandleAggregatedDataCsvExporter csvExporter;
    @Autowired
    private KafkaQueueClearService kafkaQueueClearService;
    @Autowired
    private ChartAnnotationService chartAnnotationService;


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
            chartAnnotationService.processCandle(candleEntity, "created");
            impulseZoneService.verifyZoneCorrectness(candleEntity);
            if (candleEntity.getOpen() >= candleEntity.getClose()) {
                tradeSimulationService.processActiveTrade(candleEntity, false);
                tradeDetectorService.findTradeOpportunity(candleEntity, false);
                tradeSimulationService.processActiveTrade(candleEntity, true);
                tradeDetectorService.findTradeOpportunity(candleEntity, true);
                liquidityService.invalidateLiquidityZones(candleEntity, true);
                liquidityService.invalidateLiquidityZones(candleEntity, false);
                swingPointService.confirmSwingPointIfAny(candleEntity, true);
                breakOfStructureService.checkForBreakOfStructure(candleEntity, true);
                swingPointService.checkForSwingPoint(candleEntity, true);
                swingPointService.confirmSwingPointIfAny(candleEntity, false);
                breakOfStructureService.checkForBreakOfStructure(candleEntity, false);
                swingPointService.checkForSwingPoint(candleEntity, false);
            } else {
                tradeSimulationService.processActiveTrade(candleEntity, true);
                tradeDetectorService.findTradeOpportunity(candleEntity, true);
                tradeSimulationService.processActiveTrade(candleEntity, false);
                tradeDetectorService.findTradeOpportunity(candleEntity, false);
                liquidityService.invalidateLiquidityZones(candleEntity, false);
                liquidityService.invalidateLiquidityZones(candleEntity, true);
                swingPointService.confirmSwingPointIfAny(candleEntity, false);
                breakOfStructureService.checkForBreakOfStructure(candleEntity, false);
                swingPointService.checkForSwingPoint(candleEntity, false);
                swingPointService.confirmSwingPointIfAny(candleEntity, true);
                breakOfStructureService.checkForBreakOfStructure(candleEntity, true);
                swingPointService.checkForSwingPoint(candleEntity, true);
            }
            // trendService.updateTrendStrength(candleEntity);
            volatilityCalculationService.calculateVolatility(candleEntity);
            eventCount++;

            // Store the aggregated candle data for later processing
            saveCandleAggregatedData(candleEntity);

            // After all events are processed, collect initial candle data
            if (eventCount == totalEvents && stockSymbol != null) {
                proceessEndOfEvents();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCandleAggregatedData(CandleEntity candleEntity) {

        // How do I make it understand that the timeframe i am trading is 1h?
        if (Objects.equals(candleEntity.getTimeframe(), "1d")) {
            CandleAggregatedData candleAggregatedData = candleDataAggregationService.generateInitialAggregatedData(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), candleEntity.getCandleTimestamp());
            CandleAggregatedDataEntity aggregatedEntity = candleAggregatedDataMapper.toEntity(candleAggregatedData);
            candleAggregatedDataRepository.save(aggregatedEntity);
        }
    }

    private void proceessEndOfEvents() {
        // TODO: How do I make it understand that the timeframe i am trading is 1h?
        List<CandleAggregatedDataEntity> allAggregatedData = candleAggregatedDataRepository.findByStockSymbolAndTimeframeOrderByTimestamp(stockSymbol, "1d");
        List<CandleAggregatedData> finalData = allAggregatedData.stream().map(
                candleAggregatedDataMapper::fromEntity
        ).toList();

        // TODO: Optimise this, don't store in memory
        List<CandleAggregatedData> finalProcessedData = candleDataAggregationService.fillTradeInformation(finalData);

        // Export the final processed data to CSV
        String csvFilePath = csvExporter.generateFilePath(stockSymbol, "data/processed");
        csvExporter.exportToCsv(finalProcessedData, csvFilePath);
        System.out.println("Exported aggregated data for " + stockSymbol + " to: " + csvFilePath);

        cleanUpData();

        eventCount = 0;
    }

    private void cleanUpData() {

        // TODO: Look at how we can reuse the stored data instead of fetching from api every time
        candleRepository.deleteAllInBatch();

        // Clear repositories
        swingPointRepository.deleteAllInBatch();
        breakOfStructureRepository.deleteAllInBatch();
        zoneRepository.deleteAllInBatch();
        trendRepository.deleteAllInBatch();
        volatilityRepository.deleteAllInBatch();
        tradeRepository.deleteAllInBatch();
        liquidityRepository.deleteAllInBatch();
        liquiditySweepRepository.deleteAllInBatch();
        candleAggregatedDataRepository.deleteAllInBatch();

        // TODO: Update this logic to clear old messages from Kafka queues
        System.out.println("Clearing Kafka queues after processing completion...");
        kafkaQueueClearService.clearAllQueues();
    }
}
