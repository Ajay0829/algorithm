package com.market.streamline.controller;

import com.market.streamline.kafka.candle.CandleEventConsumer;
import com.market.streamline.kafka.candle.CandleEventProducer;
import com.market.streamline.kafka.model.CandleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/data")
public class DataProcessingController {

    private static final Logger LOGGER = Logger.getLogger(DataProcessingController.class.getName());

    @Autowired
    private CandleEventConsumer candleEventConsumer;

    @Autowired
    private CandleEventProducer candleEventProducer;

    private static final String DEFAULT_DATA_DIRECTORY = "data";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");

    @PostMapping("/process-all-stocks")
    public String processAllStocks() {
        ExecutorService executorService = Executors.newFixedThreadPool(20); // Limit to 20 concurrent tasks

        try {
            File originalDir = new File(DEFAULT_DATA_DIRECTORY + "/original");
            if (!originalDir.exists() || !originalDir.isDirectory()) {
                return "Original data directory not found: " + originalDir.getAbsolutePath();
            }

            Set<String> stockSymbols = getStockSymbols(originalDir);

            List<Future<?>> futures = new ArrayList<>();

            for (String symbol : stockSymbols) {
                futures.add(executorService.submit(() -> {
                    try {
                        processStock(symbol, originalDir); // Process events for this stock
                    } catch (Exception e) {
                        LOGGER.severe("Error processing stock " + symbol + ": " + e.getMessage());
                    }
                }));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get(); // Wait for task completion
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.severe("Thread interrupted while processing stocks");
                } catch (ExecutionException e) {
                    LOGGER.severe("Error during stock processing: " + e.getCause().getMessage());
                }
            }

            return "Successfully processed all stocks in directory: " + originalDir.getAbsolutePath();

        } catch (Exception e) {
            LOGGER.severe("Error processing stocks: " + e.getMessage());
            return "Error processing stocks: " + e.getMessage();
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    private Set<String> getStockSymbols(File originalDir) {
        Set<String> symbols = new HashSet<>();
        File[] files = originalDir.listFiles((dir, name) -> name.endsWith(".csv"));

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // Extract symbol from filename (e.g., "ACC_15m.csv" -> "ACC")
                String symbol = fileName.substring(0, fileName.lastIndexOf('_'));
                symbols.add(symbol);
            }
        }

        return symbols;
    }

    protected void processStock(String symbol, File originalDir) throws IOException {
        System.out.println("Processing stock: " + symbol);

        List<CandleEvent> allCandles = new ArrayList<>();

//        // Read 15m data
//        File file15m = new File(originalDir, symbol + "_15m.csv");
//        if (file15m.exists()) {
//            allCandles.addAll(readCandlesFromFile(file15m, symbol, "15m"));
//        }

        // Read 1h data
        File file1h = new File(originalDir, symbol + "_1h.csv");
        if (file1h.exists()) {
            allCandles.addAll(readCandlesFromFile(file1h, symbol, "1h"));
        }

        // Sort all candles by end timestamp, with lower timeframe first for overlaps
        allCandles.sort((c1, c2) -> {
            LocalDateTime dt1 = LocalDateTime.parse(c1.getCandleTimestamp(), DATE_FORMATTER);
            LocalDateTime dt2 = LocalDateTime.parse(c2.getCandleTimestamp(), DATE_FORMATTER);

            // Calculate end times based on timeframe
            long end1 = dt1.atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli() + getTimeframeToMillis(c1.getTimeframe());
            long end2 = dt2.atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli() + getTimeframeToMillis(c2.getTimeframe());

            // First sort by end time
            int endTimeComparison = Long.compare(end1, end2);
            if (endTimeComparison != 0) {
                return endTimeComparison;
            }

            // If end times are equal, prioritize lower timeframe (15m before 1h)
            return Integer.compare(getTimeframeToMinutes(c1.getTimeframe()), getTimeframeToMinutes(c2.getTimeframe()));
        });

        CountDownLatch countDownLatch = new CountDownLatch(allCandles.size());

        // Set total events in consumer
        candleEventConsumer.setTotalEvents(countDownLatch, symbol);

        // Send all candles to Kafka producer
        for (CandleEvent candle : allCandles) {
            candleEventProducer.sendCandleEvent(candle);
        }

        System.out.println("Total candles for " + symbol + ": " + allCandles.size());

        try {
            countDownLatch.await();
            System.out.println("========================= All candle events processed for stock: " + symbol);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe("Interrupted while waiting for events to complete for stock: " + symbol);
        }
    }

    private List<CandleEvent> readCandlesFromFile(File file, String symbol, String timeframe) throws IOException {
        List<CandleEvent> candles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Skip header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        String datetime = parts[0].trim();
                        double open = Double.parseDouble(parts[1].trim());
                        double high = Double.parseDouble(parts[2].trim());
                        double low = Double.parseDouble(parts[3].trim());
                        double close = Double.parseDouble(parts[4].trim());
                        double volume = Double.parseDouble(parts[5].trim());

                        CandleEvent candle = new CandleEvent(symbol, timeframe, datetime, open, high, low, close, volume);
                        candles.add(candle);

                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line in file " + file.getName() + ": " + line);
                    }
                }
            }
        }

        return candles;
    }

    private long getTimeframeToMillis(String timeframe) {
        switch (timeframe) {
            case "15m":
                return 15 * 60 * 1000;
            case "1h":
                return 60 * 60 * 1000;
            default:
                return 0;
        }
    }

    private int getTimeframeToMinutes(String timeframe) {
        switch (timeframe) {
            case "15m":
                return 15;
            case "1h":
                return 60;
            default:
                return Integer.MAX_VALUE;
        }
    }
}
