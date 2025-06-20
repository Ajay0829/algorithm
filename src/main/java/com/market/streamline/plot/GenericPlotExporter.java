package com.market.streamline.plot;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.SwingPointRepository;
import com.market.streamline.repository.BreakOfStructureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GenericPlotExporter {
    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private SwingPointRepository swingPointRepository;
    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;

    public void exportPlotCsv(String stockSymbol, String timeframe, String outputPath) {
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

        // Fetch all BOS events for this symbol/timeframe
        List<BreakOfStructure> bosEvents = breakOfStructureRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        // Map BOS candle timestamp to weak swing timestamp
        Map<LocalDate, String> bosToWeakSwing = bosEvents.stream()
                .collect(Collectors.toMap(
                        bos -> bos.getCandleTimestamp().toLocalDate(),
                        bos -> bos.getWeakSwingPoint() != null ? bos.getWeakSwingPoint().getCandleTimestamp().toLocalDate().toString() : "",
                        (existing, replacement) -> existing // handle duplicates if any
                ));

        Map<LocalDate, String> bosToSwingType = bosEvents.stream()
                .collect(Collectors.toMap(
                        bos -> bos.getCandleTimestamp().toLocalDate(),
                        BreakOfStructure::getType,
                        (existing, replacement) -> existing // handle duplicates if any
                ));

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("timestamp,open,high,low,close,volume,swing_high,swing_low,break_of_structure,type\n");
            for (CandleEntity candle : candles) {
                String ts = candle.getCandleTimestamp().toLocalDate().toString();
                String open = String.format("%.2f", candle.getOpen());
                String high = String.format("%.2f", candle.getHigh());
                String low = String.format("%.2f", candle.getLow());
                String close = String.format("%.2f", candle.getClose());
                String volume = String.format("%.0f", candle.getVolume());
                String swingHigh = swingHighDates.contains(ts) ? high : "";
                String swingLow = swingLowDates.contains(ts) ? low : "";
                String breakOfStructure = bosToWeakSwing.getOrDefault(candle.getCandleTimestamp().toLocalDate(), "");
                String type = bosToSwingType.getOrDefault(candle.getCandleTimestamp().toLocalDate(), "");
                writer.write(String.join(",",
                        ts, open, high, low, close, volume, swingHigh, swingLow, breakOfStructure, type
                ) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

