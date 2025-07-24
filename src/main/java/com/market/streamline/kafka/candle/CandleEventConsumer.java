package com.market.streamline.kafka.candle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.database.DatabaseContextHolder;
import com.market.streamline.data.StockState;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import com.market.streamline.kafka.model.CandleEvent;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.*;
import com.market.streamline.service.*;
import com.market.streamline.aggregation.CandleAggregatedDataCsvExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

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
    private ZoneRepository zoneRepository;
    @Autowired
    private TrendRepository trendRepository;
    @Autowired
    private TradeDetectorService tradeDetectorService;
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
    private CandleAggregatedDataRepository candleAggregatedDataRepository;
    @Autowired
    private CandleAggregatedDataCsvExporter csvExporter;
    @Autowired
    private ChartAnnotationService chartAnnotationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MarketIndicatorsCalculationService marketIndicatorsCalculationService;
    @Autowired
    private MarketIndicatorsRepository marketIndicatorsRepository;

    private final ConcurrentHashMap<String, StockState> stockStateMap = new ConcurrentHashMap<>();


    public void setTotalEvents(CountDownLatch countDownLatch, String symbol) {
        System.out.println("setTotalEvents: total= " + countDownLatch.getCount() + ", symbol= " + symbol +  ", timestamp= " + LocalDateTime.now());
        stockStateMap.put(symbol, new StockState(countDownLatch));
    }

    @KafkaListener(topics = "candle-added", groupId = "${candle.kafka.consumer.group-id}", concurrency = "20")
    public void listenCandleAdded(List<String> messages) {
        try {

            for (String message : messages) {
                CandleEvent candleEvent = objectMapper.readValue(message, CandleEvent.class);
                CandleEntity candleEntity = getCandleEntity(candleEvent);

                String stockSymbol = candleEntity.getStockSymbol();
                int dbIndex = Math.abs(stockSymbol.hashCode() % 20);
                String databaseKey = "my_database_" + (dbIndex + 1);
                DatabaseContextHolder.setCurrentDatabase(databaseKey);

                try {
                    // Perform database operations
                    chartAnnotationService.processCandle(candleEntity, "created");
                    processCandle(candleEntity);

                    StockState stockState = stockStateMap.get(stockSymbol);
                    if (stockState != null) {
                        stockState.incrementCurrentEvents();

                        if (stockState.getCountDownLatch().getCount() == 0) {
                            processEndOfEvents(stockSymbol);
                            System.out.println("Completed processing all candle events for stock: [" + stockSymbol + "], Time taken: "
                                    + Duration.between(stockState.getTimestamp(), LocalDateTime.now()).toMinutes());
                            stockStateMap.remove(stockSymbol);
                        }
                    }
                } finally {
                    DatabaseContextHolder.clear();
                }
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
        marketIndicatorsCalculationService.calculateIndicators(candleEntity);
    }

    private void processCandle(CandleEntity candleEntity) {
        candleRepository.save(candleEntity);
        boolean redCandle = isRedCandle(candleEntity);
        processCandlePoint(candleEntity, redCandle); // If Red Candle, Process High else Process Low
        processCandlePoint(candleEntity, !redCandle); // If Red Candle, Process Low next else Process High next
        processCandleClosure(candleEntity); // Process the closure of the candle
    }

    private static CandleEntity getCandleEntity(CandleEvent candleEvent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
        LocalDateTime candleTime = LocalDateTime.parse(candleEvent.getCandleTimestamp(), formatter);
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

    private void processEndOfEvents(String stockSymbol) {
        // TODO: Configure for multiple timeframes
        List<CandleAggregatedDataEntity> allAggregatedData = candleAggregatedDataRepository.findByStockSymbolAndTimeframeOrderByTimestamp(stockSymbol, "1h");

        // Export the final processed data to CSV
        String csvFilePath = csvExporter.generateFilePath(stockSymbol, "data/processed");
        csvExporter.exportToCsv(allAggregatedData, csvFilePath);
        System.out.println("Exported aggregated data for " + stockSymbol + " to: " + csvFilePath);
        cleanUpData(stockSymbol);
    }

    private void cleanUpData(String stockSymbol) {

        // TODO: Look at how we can reuse the stored data instead of fetching from api every time
        candleRepository.deleteByStockSymbolInBatch(stockSymbol);
        swingPointRepository.deleteByStockSymbolInBatch(stockSymbol);
        breakOfStructureRepository.deleteByStockSymbolInBatch(stockSymbol);
        zoneRepository.deleteByStockSymbolInBatch(stockSymbol);
        trendRepository.deleteByStockSymbolInBatch(stockSymbol);
        tradeRepository.deleteByStockSymbolInBatch(stockSymbol);
        liquidityRepository.deleteByStockSymbolInBatch(stockSymbol);
        liquiditySweepRepository.deleteByStockSymbolInBatch(stockSymbol);
        candleAggregatedDataRepository.deleteByStockSymbolInBatch(stockSymbol);
        marketIndicatorsRepository.deleteByStockSymbolInBatch(stockSymbol);
    }
}
