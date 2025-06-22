package com.market;

import com.market.external.polygon.dto.Candle;
import com.market.external.polygon.service.PolygonService;
import com.market.streamline.kafka.CandleEventConsumer;
import com.market.streamline.kafka.CandleEventProducer;
import com.market.streamline.model.CandleEvent;
import com.market.streamline.util.CsvCandleLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component
public class MarketBatchStartupRunner {
    @Autowired
    private CandleEventProducer candleEventProducer;
    @Autowired
    private CandleEventConsumer candleEventConsumer;
    @Autowired
    private PolygonService polygonService;


    public void runBatchProcess(String stockSymbol, String timeframe, String from, String to) {
        List<CandleEvent> events = runForPolygon(stockSymbol, timeframe, from, to);
        candleEventConsumer.setTotalEvents(events.size(), stockSymbol);
        events.forEach(candleEvent -> candleEventProducer.sendCandleEvent(candleEvent));
        System.out.println(events.size() + " events published to Kafka topic 'candle-added' for " + stockSymbol);
    }

    public List<CandleEvent> runForCsv() {
        // Single file mode
        String csvFilePath = "/Users/aramapuram/AlgorithmicTrading/data/actual/Stocks/googl.us.csv";
        String fileName = csvFilePath.substring(csvFilePath.lastIndexOf('/') + 1);
        String stockSymbol = fileName.split("\\.")[0];

        // Count total events (excluding header)
        int totalEvents = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while (br.readLine() != null) totalEvents++;
            totalEvents--; // exclude header
        } catch (Exception e) {
            e.printStackTrace();
        }
        candleEventConsumer.setTotalEvents(totalEvents, stockSymbol);
        return CsvCandleLoader.loadFromCsv(csvFilePath);
    }

    public List<CandleEvent> runForPolygon(String stockSymbol, String timeframe, String from, String to) {

        List<Candle> candles = polygonService.getAggregates(stockSymbol, timeframe, from, to).getResults();
        return candles.stream()
                .map(candle -> new CandleEvent(
                        stockSymbol,
                        timeframe,
                        Instant.ofEpochMilli(candle.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString(),
                        candle.getOpen(),
                        candle.getHigh(),
                        candle.getLow(),
                        candle.getClose(),
                        candle.getVolume()))
                .toList();
    }
}
