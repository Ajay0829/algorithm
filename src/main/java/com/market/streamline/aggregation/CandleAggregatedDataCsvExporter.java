package com.market.streamline.aggregation;

import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CandleAggregatedDataCsvExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void exportToCsv(List<CandleAggregatedDataEntity> aggregatedDataList, String filePath) {
        try {
            // Ensure parent directories exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("Created directory: " + parentDir.getAbsolutePath());
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
                // Header uses ONLY fields populated by CandleDataAggregationService
                String[] header = {
                        "stock_symbol",
                        "timeframe",
                        "candle_timestamp",
                        // BOS & Liquidity sweep
//                        "bos_direction",
//                        "bos_volume",
//                        "liquidity_sweep_direction",
                        // Zone context (same & opposing)
//                        "same_zone_strength",
//                        "same_zone_volume",
//                        "zone_taps",
//                        "opposing_zone_distance",
//                        "opposing_zone_volume",
//                        "opposing_zone_strength",
                        // Liquidity distances
//                        "same_liquidity_distance",
//                        "opposing_liquidity_distance",
                        // Indicators
//                        "volatility_14",
//                        "volatility_50",
//                        "volatility_200",
//                        "volume_14",
//                        "volume_50",
//                        "volume_200",
//                        "rsi14",
//                        "rsi50",
                        // Trade context
                        "trade",
//                        "resilience",
//                        "half_life",
//                        "time_to_return",
//                        "same_direction_max_move",
                        "trade_result"
                };
                writer.writeNext(header);

                for (CandleAggregatedDataEntity data : aggregatedDataList) {
                    String[] row = new String[] {
                            s(data.getStockSymbol()),
                            s(data.getTimeframe()),
                            data.getCandleTimestamp() != null ? data.getCandleTimestamp().format(DATE_TIME_FORMATTER) : "",
                            // BOS & Liquidity sweep
//                            s(data.getBosDirection()),
//                            n(data.getBosVolume()),
//                            s(data.getLiquiditySweepDirection()),
                            // Zone context
//                            n(data.getSameZoneStrength()),
//                            n(data.getSameZoneVolume()),
//                            n(data.getZoneTaps()),
//                            n(data.getOpposingZoneDistance()),
//                            n(data.getOpposingZoneVolume()),
//                            n(data.getOpposingZoneStrength()),
                            // Liquidity distances
//                            n(data.getSameLiquidityDistance()),
//                            n(data.getOpposingLiquidityDistance()),
                            // Indicators
//                            n(data.getVolatility14()),
//                            n(data.getVolatility50()),
//                            n(data.getVolatility200()),
//                            n(data.getVolume14()),
//                            n(data.getVolume50()),
//                            n(data.getVolume200()),
//                            n(data.getRsi14()),
//                            n(data.getRsi50()),
                            // Trade context
                            s(data.getTrade()),
//                            n(data.getResilience()),
//                            n(data.getHalfLife()),
//                            n(data.getTimeToReturn()),
//                            n(data.getSameDirectionMaxMove()),
                            s(data.getTradeResult())
                    };
                    writer.writeNext(row);
                }

                System.out.println("Successfully exported " + aggregatedDataList.size() + " records to: " + filePath);
            } catch (IOException e) {
                System.err.println("Error exporting aggregated data to CSV: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error creating directories for CSV export: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String generateFilePath(String stockSymbol, String outputDirectory) {
        if (outputDirectory == null || outputDirectory.isEmpty()) {
            outputDirectory = "data/processed_index";
        }
        return outputDirectory + "/" + stockSymbol + "_aggregated_data.csv";
    }

    private static String s(String v) { return v == null ? "" : v; }
    private static String n(Object v) { return v == null ? "" : String.valueOf(v); }
}
