package com.market.streamline.kafka.candle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.aggregation.CandleAggregatedData;
import com.market.streamline.aggregation.CandleAggregatedDataMapper;
import com.market.streamline.aggregation.CandleDataAggregationService;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import com.market.streamline.kafka.KafkaQueueClearService;
import com.market.streamline.kafka.model.CandleEvent;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.*;
import com.market.streamline.service.*;
import com.market.streamline.aggregation.CandleAggregatedDataCsvExporter;
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
    private SwingPointService swingPointService;
    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private SwingPointRepository swingPointRepository;
    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private int eventCount = 0;
    private int totalEvents = 0;
    private String stockSymbol = null;


    public void setTotalEvents(int total, String symbol) {
        this.totalEvents = total;
        this.stockSymbol = symbol;
    }

    @KafkaListener(topics = "candle-added", groupId = "feature-extractor-group")
    public void listenCandleAdded(String message) {
        try {
            CandleEvent candleEvent = objectMapper.readValue(message, CandleEvent.class);
            CandleEntity candleEntity = getCandleEntity(candleEvent);
            candleRepository.save(candleEntity);
            chartAnnotationService.processCandle(candleEntity, "created");
            processCandle(candleEntity);
            eventCount++;

            // After all events are processed, collect initial candle data
            if (eventCount == totalEvents && stockSymbol != null) {
                processEndOfEvents();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRedCandle(CandleEntity candleEntity) {
        return candleEntity.getOpen() > candleEntity.getClose();
    }

    private void processCandlePoint(CandleEntity candleEntity, boolean isHighCheck) {
        tradeSimulationService.processActiveTrade(candleEntity, isHighCheck);
        impulseZoneService.invalidateZones(candleEntity, isHighCheck);
        tradeDetectorService.findTradeOpportunity(candleEntity, isHighCheck);
        liquidityService.invalidateLiquidityZones(candleEntity, isHighCheck);
        swingPointService.confirmSwingPointIfAny(candleEntity, isHighCheck);
        breakOfStructureService.checkForBreakOfStructure(candleEntity, isHighCheck);
        swingPointService.checkForSwingPoint(candleEntity, isHighCheck);
    }

    private void processCandleClosure(CandleEntity candleEntity) {
        impulseZoneService.verifyZoneCorrectness(candleEntity);
        volatilityCalculationService.calculateVolatility(candleEntity);
//        saveCandleAggregatedData(candleEntity);
    }

    private void processCandle(CandleEntity candleEntity) {
        boolean redCandle = isRedCandle(candleEntity);
        processCandlePoint(candleEntity, redCandle); // If Red Candle, Process High else Process Low
        processCandlePoint(candleEntity, !redCandle); // If Red Candle, Process Low next else Process High next
        processCandleClosure(candleEntity); // Process the closure of the candle
    }

    private static CandleEntity getCandleEntity(CandleEvent candleEvent) {
        LocalDateTime candleTime = LocalDateTime.parse(candleEvent.getCandleTimestamp(), DateTimeFormatter.ISO_DATE_TIME);
        return new CandleEntity(
                candleEvent.getStockSymbol(),
                candleEvent.getTimeframe(),
                candleTime,
                candleEvent.getOpen(),
                candleEvent.getClose(),
                candleEvent.getHigh(),
                candleEvent.getLow(),
                candleEvent.getVolume()
        );
    }

    private void saveCandleAggregatedData(CandleEntity candleEntity) {

        // TODO: Configure for multiple timeframes
        if (Objects.equals(candleEntity.getTimeframe(), "1d")) {
            CandleAggregatedData candleAggregatedData = candleDataAggregationService.generateInitialAggregatedData(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), candleEntity.getCandleTimestamp());
            CandleAggregatedDataEntity aggregatedEntity = candleAggregatedDataMapper.toEntity(candleAggregatedData);
            candleAggregatedDataRepository.save(aggregatedEntity);
        }
    }

    private void processEndOfEvents() {
        // TODO: Configure for multiple timeframes
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
