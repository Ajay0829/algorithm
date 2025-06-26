package com.market.streamline.plot;

import com.market.streamline.entity.*;
import com.market.streamline.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private TrendRepository trendRepository;

    public void exportPlotCsv(String stockSymbol, String timeframe, String outputPath) {
        List<CandleEntity> candles = candleRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        List<SwingPoint> swingPoints = swingPointRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        List<Zone> zones = zoneRepository.findByStockSymbol(stockSymbol);
        // Use only the date part for matching
        Set<String> swingHighDates = swingPoints.stream()
                .filter(sp -> "HIGH".equalsIgnoreCase(sp.getSwingType()))
                .map(sp -> getLocalDateTimeFromCandleTimestamp(sp.getCandleTimestamp()))
                .collect(Collectors.toSet());
        Set<String> swingLowDates = swingPoints.stream()
                .filter(sp -> "LOW".equalsIgnoreCase(sp.getSwingType()))
                .map(sp -> getLocalDateTimeFromCandleTimestamp(sp.getCandleTimestamp()))
                .collect(Collectors.toSet());
        Set<String> majorSwingHighDates = swingPoints.stream()
                .filter(sp -> "HIGH".equalsIgnoreCase(sp.getSwingType()) && sp.getIsMajor())
                .map(sp -> getLocalDateTimeFromCandleTimestamp(sp.getCandleTimestamp()))
                .collect(Collectors.toSet());

        Set<String> majorSwingLowDates = swingPoints.stream()
                .filter(sp -> "LOW".equalsIgnoreCase(sp.getSwingType()) && sp.getIsMajor())
                .map(sp -> getLocalDateTimeFromCandleTimestamp(sp.getCandleTimestamp()))
                .collect(Collectors.toSet());

        Map<String, List<Zone>> zoneData = zones.stream()
                .collect(Collectors.groupingBy(
                        zone -> getLocalDateTimeFromCandleTimestamp(zone.getCandleTimestamp())
                ));

        List<Trend> trends = trendRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);

        // Fetch all BOS events for this symbol/timeframe
        List<BreakOfStructure> bosEvents = breakOfStructureRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        // Map BOS candle timestamp to weak swing timestamp
        Map<String, String> bosToWeakSwing = bosEvents.stream()
                .collect(Collectors.toMap(
                        bos -> getLocalDateTimeFromCandleTimestamp(bos.getCandleTimestamp()),
                        bos -> bos.getWeakSwingPoint() != null ? getLocalDateTimeFromCandleTimestamp(bos.getWeakSwingPoint().getCandleTimestamp()) : "",
                        (existing, replacement) -> existing // handle duplicates if any
                ));

        Map<String, String> bosToSwingType = bosEvents.stream()
                .collect(Collectors.toMap(
                        bos -> getLocalDateTimeFromCandleTimestamp(bos.getCandleTimestamp()),
                        BreakOfStructure::getType,
                        (existing, replacement) -> existing // handle duplicates if any
                ));

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("timestamp,open,high,low,close,volume,swing_high,swing_low,is_major,break_of_structure,type,zone,near_point,far_point,trend_type\n");
            for (CandleEntity candle : candles) {
                String ts = getLocalDateTimeFromCandleTimestamp(candle.getCandleTimestamp());
                String open = String.format("%.2f", candle.getOpen());
                String high = String.format("%.2f", candle.getHigh());
                String low = String.format("%.2f", candle.getLow());
                String close = String.format("%.2f", candle.getClose());
                String volume = String.format("%.0f", candle.getVolume());
                String swingHigh = swingHighDates.contains(ts) ? high : "";
                String swingLow = swingLowDates.contains(ts) ? low : "";
                String isMajor = majorSwingHighDates.contains(ts) ? "true" : (majorSwingLowDates.contains(ts) ? "true" : "false");
                String breakOfStructure = bosToWeakSwing.getOrDefault(getLocalDateTimeFromCandleTimestamp(candle.getCandleTimestamp()), "");
                String type = bosToSwingType.getOrDefault(getLocalDateTimeFromCandleTimestamp(candle.getCandleTimestamp()), "");
                List<Zone> zoneList = zoneData.getOrDefault(getLocalDateTimeFromCandleTimestamp(candle.getCandleTimestamp()), List.of());
                String zoneType = "";
                String nearPoint = "";
                String farPoint = "";
                if (zoneList.size() == 1) {
                    Zone firstZone = zoneList.get(0);
                    zoneType = firstZone.getZoneType() != null ? firstZone.getZoneType() : "";
                    nearPoint = firstZone.getNearPoint() != null ? String.format("%.2f", firstZone.getNearPoint()) : "";
                    farPoint = firstZone.getFarPoint() != null ? String.format("%.2f", firstZone.getFarPoint()) : "";
                }
                // Find latest trend from trends list before or at candle timestamp
                Trend latestTrend = null;
                for (Trend t : trends) {
                    if ((t.getCandleTimestamp().isBefore(candle.getCandleTimestamp()) || t.getCandleTimestamp().isEqual(candle.getCandleTimestamp())) &&
                        (latestTrend == null || t.getCandleTimestamp().isAfter(latestTrend.getCandleTimestamp()))) {
                        latestTrend = t;
                    }
                }
                String trendType = latestTrend != null ? latestTrend.getType() : "SIDEWAYS";
                writer.write(String.join(",",
                        ts, open, high, low, close, volume, swingHigh, swingLow, isMajor, breakOfStructure, type, zoneType, nearPoint, farPoint, trendType
                ) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLocalDateTimeFromCandleTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter);
    }
}
