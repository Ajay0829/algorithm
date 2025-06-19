package com.market.streamline.plot;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.SwingPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SwingPointPlotExporter {
    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private SwingPointRepository swingPointRepository;

    public void exportSwingPointsCsv(String stockSymbol, String timeframe, String outputPath) {
        List<CandleEntity> candles = candleRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        List<SwingPoint> swingPoints = swingPointRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        // Use only the date part for matching
        Set<String> swingHighDates = swingPoints.stream()
                .filter(sp -> "HIGH".equalsIgnoreCase(sp.getSwingType()))
                .map(sp -> sp.getCandleTimestamp().toLocalDate().toString())
                .collect(Collectors.toSet());
        Set<String> swingLowDates = swingPoints.stream()
                .filter(sp -> "LOW".equalsIgnoreCase(sp.getSwingType()))
                .map(sp -> sp.getCandleTimestamp().toLocalDate().toString())
                .collect(Collectors.toSet());
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("timestamp,open,high,low,close,volume,swing_high,swing_low\n");
            for (CandleEntity candle : candles) {
                String ts = candle.getCandleTimestamp().toLocalDate().toString();
                String open = String.format("%.2f", candle.getOpen());
                String high = String.format("%.2f", candle.getHigh());
                String low = String.format("%.2f", candle.getLow());
                String close = String.format("%.2f", candle.getClose());
                String volume = String.format("%.0f", candle.getVolume());
                String swingHigh = swingHighDates.contains(ts) ? high : "";
                String swingLow = swingLowDates.contains(ts) ? low : "";
                writer.write(String.join(",",
                    ts, open, high, low, close, volume, swingHigh, swingLow
                ) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


