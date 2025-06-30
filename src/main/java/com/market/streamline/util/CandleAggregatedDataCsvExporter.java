package com.market.streamline.util;

import com.market.streamline.service.CandleAggregatedData;
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

    public void exportToCsv(List<CandleAggregatedData> aggregatedDataList, String filePath) {
        try {
            // Create directories if they don't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                System.out.println("Created directory: " + parentDir.getAbsolutePath());
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {

                // Write header
                String[] header = {
                    "stock_symbol", "timeframe", "candle_timestamp", "open", "close", "high", "low", "volume",
                    "last_swing_high", "last_swing_low", "supply_price", "demand_price",
                    "last_liquidity_sweep_type", "bos_direction", "buy_liquidity", "sell_liquidity",
                    "volatility", "trade", "entry_price", "target_price", "stop_loss_price", "trade_result"
                };
                writer.writeNext(header);

                // Write data rows
                for (CandleAggregatedData data : aggregatedDataList) {
                    String[] row = {
                        data.getStockSymbol(),
                        data.getTimeframe(),
                        data.getCandleTimestamp() != null ? data.getCandleTimestamp().format(DATE_TIME_FORMATTER) : "",
                        data.getOpen() != null ? data.getOpen().toString() : "",
                        data.getClose() != null ? data.getClose().toString() : "",
                        data.getHigh() != null ? data.getHigh().toString() : "",
                        data.getLow() != null ? data.getLow().toString() : "",
                        data.getVolume() != null ? data.getVolume().toString() : "",
                        String.valueOf(data.getLastSwingHigh()),
                        String.valueOf(data.getLastSwingLow()),
                        String.valueOf(data.getSupplyPrice()),
                        String.valueOf(data.getDemandPrice()),
                        String.valueOf(data.isLastLiquiditySweepType()),
                        String.valueOf(data.isBosDirection()),
                        String.valueOf(data.getBuyLiquidity()),
                        String.valueOf(data.getSellLiquidity()),
                        String.valueOf(data.getVolatility()),
                        data.getTrade() != null ? data.getTrade() : "",
                        String.valueOf(data.getEntryPrice()),
                        String.valueOf(data.getTargetPrice()),
                        String.valueOf(data.getStopLossPrice()),
                        String.valueOf(data.isTradeResult())
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
            outputDirectory = "data/processed";
        }
        return outputDirectory + "/" + stockSymbol + "_aggregated_data.csv";
    }
}
