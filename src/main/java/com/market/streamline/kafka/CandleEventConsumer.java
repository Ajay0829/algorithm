package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.model.CandleEvent;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.service.FeatureExtractionService;
import com.market.streamline.service.SwingPointService;
import com.market.streamline.plot.SwingPointPlotExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CandleEventConsumer {

    @Autowired
    private FeatureExtractionService featureExtractionService;

    @Autowired
    private SwingPointService swingPointService;

    @Autowired
    private SwingPointPlotExporter swingPointPlotExporter;

    @Autowired
    private CandleRepository candleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private int eventCount = 0;
    private int totalEvents = 0;
    private String stockSymbol = null;

    public void setTotalEvents(int total, String symbol) {
        this.totalEvents = total;
        this.stockSymbol = symbol;
    }

    @KafkaListener(topics = "candle-added", groupId = "feature-extractor-group")
    public void listen(String message) {
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

            Optional<SwingPoint> swingPoint = swingPointService.checkForSwingPoint(candleEntity);
            eventCount++;
            // After all events are processed, export for plotting
            if (eventCount == totalEvents && stockSymbol != null) {
                swingPointPlotExporter.exportSwingPointsCsv(stockSymbol, "1D", stockSymbol + "_swing_points.csv");
                System.out.println("Exported swing points for " + stockSymbol);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

