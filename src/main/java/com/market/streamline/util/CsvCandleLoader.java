package com.market.streamline.util;

import com.market.streamline.model.CandleEvent;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvCandleLoader {

    public static List<CandleEvent> loadFromCsv(String filePath) {
        List<CandleEvent> events = new ArrayList<>();
        // Extract stock symbol from filename (e.g., abc.us.csv -> abc)
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        String stockSymbol = fileName.split("\\.")[0];
        String timeframe = "1D";
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            boolean headerSkipped = false;
            while ((line = reader.readNext()) != null) {
                if (!headerSkipped) { headerSkipped = true; continue; }
                CandleEvent event = new CandleEvent();
                event.setStockSymbol(stockSymbol);
                event.setTimeframe(timeframe);
                // Parse date string (e.g., "1970-01-02") to LocalDateTime at start of day
                LocalDate date = LocalDate.parse(line[0], DateTimeFormatter.ISO_LOCAL_DATE);
                event.setCandleTimestamp(date.atStartOfDay().toString());
                event.setOpen(Double.parseDouble(line[1]));
                event.setHigh(Double.parseDouble(line[2]));
                event.setLow(Double.parseDouble(line[3]));
                event.setClose(Double.parseDouble(line[4]));
                event.setVolume(Double.parseDouble(line[5]));
                events.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return events;
    }
}
