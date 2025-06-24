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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
        String lowerTimeframe;
        if (timeframe.equals("1h")) {
            lowerTimeframe = "15m";
        } else if (timeframe.equals("1d")) {
            lowerTimeframe = "1h";
        } else {
            throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
        List<CandleEvent> htfEvents = runForPolygon(stockSymbol, timeframe, from, to);
        List<CandleEvent> ltfEvents = runForPolygon(stockSymbol, lowerTimeframe, from, to);

        List<CandleEvent> allEvents = new ArrayList<>();
        allEvents.addAll(htfEvents);
        allEvents.addAll(ltfEvents);

        // Sort by candle end time, then by timeframe (lower first)
        allEvents.sort((a, b) -> {
            LocalDateTime aStart = LocalDateTime.parse(a.getCandleTimestamp());
            LocalDateTime bStart = LocalDateTime.parse(b.getCandleTimestamp());
            long aEnd = aStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + timeframeToMillis(a.getTimeframe());
            long bEnd = bStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + timeframeToMillis(b.getTimeframe());
            int cmp = Long.compare(aEnd, bEnd);
            if (cmp != 0) return cmp;
            return Integer.compare(timeframeToMinutes(a.getTimeframe()), timeframeToMinutes(b.getTimeframe()));
        });

        candleEventConsumer.setTotalEvents(allEvents.size(), stockSymbol);
        allEvents.forEach(candleEvent -> candleEventProducer.sendCandleEvent(candleEvent));
        System.out.println(allEvents.size() + " events published to Kafka topic 'candle-added' for " + stockSymbol);
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

        List<Candle> candles = polygonService.getAggregates(stockSymbol, timeframe, from, to);
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

    // Helper to convert timeframe string to minutes for sorting
    private int timeframeToMinutes(String tf) {
        if (tf == null) return Integer.MAX_VALUE;
        if (tf.endsWith("m")) return Integer.parseInt(tf.replace("m", ""));
        if (tf.endsWith("h")) return Integer.parseInt(tf.replace("h", "")) * 60;
        if (tf.endsWith("d")) return Integer.parseInt(tf.replace("d", "")) * 1440;
        return Integer.MAX_VALUE;
    }

    // Helper to convert timeframe string to milliseconds
    private long timeframeToMillis(String tf) {
        if (tf == null) return Long.MAX_VALUE;
        if (tf.endsWith("m")) return Integer.parseInt(tf.replace("m", "")) * 60L * 1000L;
        if (tf.endsWith("h")) return Integer.parseInt(tf.replace("h", "")) * 60L * 60L * 1000L;
        if (tf.endsWith("d")) return Integer.parseInt(tf.replace("d", "")) * 24L * 60L * 60L * 1000L;
        return Long.MAX_VALUE;
    }
}
