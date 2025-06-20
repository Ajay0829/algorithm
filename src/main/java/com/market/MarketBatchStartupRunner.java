package com.market;

import com.market.streamline.kafka.CandleEventConsumer;
import com.market.streamline.kafka.CandleEventProducer;
import com.market.streamline.model.CandleEvent;
import com.market.streamline.util.CsvCandleLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

@Component
public class MarketBatchStartupRunner implements CommandLineRunner {
    @Autowired
    private CandleEventProducer candleEventProducer;
    @Autowired
    private CandleEventConsumer candleEventConsumer;

    @Override
    public void run(String... args) {
        System.out.println("Starting MarketBatchStartupRunner");
        // Single file mode
        String csvFilePath = "/Users/aramapuram/AlgorithmicTrading/data/actual/Stocks/ko.us.csv";
        String fileName = csvFilePath.substring(csvFilePath.lastIndexOf('/') + 1);
        String stockSymbol = fileName.split("\\.")[0];
        String timeframe = "1D";

        // Count total events (excluding header)
        int totalEvents = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while (br.readLine() != null) totalEvents++;
            totalEvents--; // exclude header
        } catch (Exception e) {
            e.printStackTrace();
        }
        candleEventConsumer.setTotalEvents(totalEvents, stockSymbol);
        List<CandleEvent> events = CsvCandleLoader.loadFromCsv(csvFilePath);
        events.forEach(candleEvent -> candleEventProducer.sendCandleEvent(candleEvent));
        System.out.println("All events published for " + stockSymbol + ".");
    }
}
