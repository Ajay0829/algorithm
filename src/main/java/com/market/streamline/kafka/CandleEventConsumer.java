package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.dto.ChartCandleDTO;
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
            chartAnnotationProducer.sendAnnotation(
                    new ChartCandleDTO(
                            "candle",
                            "created",
                            new ChartCandleDTO.CandleData(
                                    candleEvent.getCandleTimestamp(),
                                    candleEvent.getOpen(),
                                    candleEvent.getHigh(),
                                    candleEvent.getLow(),
                                    candleEvent.getClose(),
                                    candleEvent.getVolume(),
                                    candleEvent.getStockSymbol(),
                                    candleEvent.getTimeframe()
                            )
                    )
            );
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
            zoneService.invalidateZones(candleEntity);
            zoneService.updateZoneStrength(candleEntity);
            eventCount++;

            // Store the aggregated candle data for later processing
            if (Objects.equals(candleEntity.getTimeframe(), "1h")) {
                CandleAggregatedData candleAggregatedData = candleDataAggregationService.generateInitialAggregatedData(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), candleEntity.getCandleTimestamp());
                CandleAggregatedDataEntity aggregatedEntity = candleAggregatedDataMapper.toEntity(candleAggregatedData);
                candleAggregatedDataRepository.save(aggregatedEntity);
            }

            // After all events are processed, collect initial candle data
            if (eventCount == totalEvents && stockSymbol != null) {
                // Retrieve all stored aggregated data for final processing
                List<CandleAggregatedDataEntity> allAggregatedData = candleAggregatedDataRepository.findByStockSymbolAndTimeframeOrderByTimestamp(stockSymbol, "1h");
                List<CandleAggregatedData> finalData = allAggregatedData.stream().map(
                        candleAggregatedDataMapper::fromEntity
                ).toList();
                List<CandleAggregatedData> finalProcessedData = candleDataAggregationService.fillTradeInformation(finalData);

                // Export the final processed data to CSV
                String csvFilePath = csvExporter.generateFilePath(stockSymbol, "data/processed");
                csvExporter.exportToCsv(finalProcessedData, csvFilePath);
                System.out.println("Exported aggregated data for " + stockSymbol + " to: " + csvFilePath);


                // Clear repositories
                candleRepository.deleteAllInBatch();
                swingPointRepository.deleteAllInBatch();
                breakOfStructureRepository.deleteAllInBatch();
                zoneRepository.deleteAllInBatch();
                trendRepository.deleteAllInBatch();
                volatilityRepository.deleteAllInBatch();
                tradeRepository.deleteAllInBatch();
                liquidityRepository.deleteAllInBatch();
                liquiditySweepRepository.deleteAllInBatch();
                candleAggregatedDataRepository.deleteAllInBatch();

                eventCount = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
