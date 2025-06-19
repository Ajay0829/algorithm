package com.market.streamline.csv;

import com.market.streamline.kafka.CandleEventProducer;
import com.market.streamline.model.CandleEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Component
public class CandleCsvReader {
    @Autowired
    private CandleEventProducer candleEventProducer;

    public void readAndPublish(String csvFilePath, String stockSymbol, String timeframe) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // skip header
                    isFirstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length < 6) continue; // basic validation
                CandleEvent event = new CandleEvent();
                event.setStockSymbol(stockSymbol);
                event.setTimeframe(timeframe);
                event.setCandleTimestamp(values[0]);
                event.setOpen(Double.parseDouble(values[1]));
                event.setHigh(Double.parseDouble(values[2]));
                event.setLow(Double.parseDouble(values[3]));
                event.setClose(Double.parseDouble(values[4]));
                event.setVolume(Double.parseDouble(values[5]));
                candleEventProducer.sendCandleEvent(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

