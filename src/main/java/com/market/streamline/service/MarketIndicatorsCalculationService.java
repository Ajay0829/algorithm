package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.lang.Math.*;

@Service
public class MarketIndicatorsCalculationService {

    private final MarketIndicatorsRepository marketIndicatorsRepository;
    private final CandleRepository candleRepository;
    private static final int RSI_14 = 14;
    private static final int RSI_50 = 50;
    private static final int WINDOW_200 = 200;
    private static final int WINDOW_50 = 50;
    private static final int WINDOW_14 = 14;

    public MarketIndicatorsCalculationService(MarketIndicatorsRepository marketIndicatorsRepository, CandleRepository candleRepository) {
        this.marketIndicatorsRepository = marketIndicatorsRepository;
        this.candleRepository = candleRepository;
    }

    public void calculateIndicators(CandleEntity candleEntity) {
        String stockSymbol = candleEntity.getStockSymbol();
        String timeframe = candleEntity.getTimeframe();

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(
                stockSymbol,
                timeframe
        );

        if (marketIndicators == null) {
            double initialVolatility = abs(candleEntity.getHigh() - candleEntity.getLow()) * 100 / candleEntity.getClose();
            marketIndicators = new MarketIndicators(
                    stockSymbol,
                    timeframe,
                    initialVolatility,
                    initialVolatility,
                    initialVolatility,
                    candleEntity.getVolume(),
                    candleEntity.getVolume(),
                    candleEntity.getVolume(),
                    null,
                    null,
                    1
            );
        } else {
            // Update average volatility
            double currentVolatility14 = marketIndicators.getVolatility14();
            double currentVolatility50 = marketIndicators.getVolatility50();
            double currentVolatility200 = marketIndicators.getVolatility200();
            CandleEntity previousCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
                    candleEntity.getStockSymbol(), candleEntity.getTimeframe(), candleEntity.getCandleTimestamp()
            );
            double previousClose = previousCandle.getClose();
            double atr = getATR(previousClose, candleEntity) * 100;
            double volume = candleEntity.getVolume();

            double currentVolume14 = marketIndicators.getVolume14();
            double currentVolume50 = marketIndicators.getVolume50();
            double currentVolume200 = marketIndicators.getVolume200();

            int totalSamples = marketIndicators.getNoOfSamples();
            double finalVolatility200, finalVolatility50, finalVolatility14, finalVolume200, finalVolume50, finalVolume14;

            finalVolatility14 = computeAverageMetric(currentVolatility14, atr, totalSamples, WINDOW_14);
            finalVolatility50 = computeAverageMetric(currentVolatility50, atr, totalSamples, WINDOW_50);
            finalVolatility200 = computeAverageMetric(currentVolatility200, atr, totalSamples, WINDOW_200);
            finalVolume14 = computeAverageMetric(currentVolume14, volume, totalSamples, WINDOW_14);
            finalVolume50 = computeAverageMetric(currentVolume50, volume, totalSamples, WINDOW_50);
            finalVolume200 = computeAverageMetric(currentVolume200, volume, totalSamples, WINDOW_200);

            marketIndicators.setVolatility14(finalVolatility14);
            marketIndicators.setVolatility50(finalVolatility50);
            marketIndicators.setVolatility200(finalVolatility200);
            marketIndicators.setVolume14(finalVolume14);
            marketIndicators.setVolume50(finalVolume50);
            marketIndicators.setVolume200(finalVolume200);

            marketIndicators.setNoOfSamples(totalSamples + 1);
        }

        // Calculate and update RSI
        Double rsi14 = calculateRSI(stockSymbol, timeframe, candleEntity, RSI_14);
        Double rsi50 = calculateRSI(stockSymbol, timeframe, candleEntity, RSI_50);
        if (rsi14 != null) {
            marketIndicators.setRsi14(rsi14);
        }
        if (rsi50 != null) {
            marketIndicators.setRsi50(rsi50);
        }

        marketIndicatorsRepository.save(marketIndicators);
    }

    double computeAverageMetric(double oldValue, double currentValue, int noOfSamples, int window) {
        int sampleSize = min(window, noOfSamples);
        return ( oldValue * (sampleSize - 1) + currentValue ) / sampleSize;
    }

    double getATR(double previousClose, CandleEntity candleEntity) {
        double volatility = max(abs(candleEntity.getHigh() - candleEntity.getLow()), abs(candleEntity.getHigh() - previousClose));
        volatility = max(volatility, abs(candleEntity.getLow() - previousClose));
        volatility /= previousClose;

        return volatility;
    }

    /**
     * Calculate RSI (Relative Strength Index) using Wilder's smoothing method (standard in trading platforms)
     */
    private Double calculateRSI(String stockSymbol, String timeframe, CandleEntity currentCandle, Integer RSI_PERIOD) {
        // Get more historical data for proper RSI calculation
        // We need at least RSI_PERIOD * 2 candles for accurate smoothing
        List<CandleEntity> recentCandles = candleRepository.findRecentCandles(
                stockSymbol,
                timeframe,
                currentCandle.getCandleTimestamp(),
                PageRequest.of(0, RSI_PERIOD * 2 + 1)
        );

        // Need at least RSI_PERIOD + 1 candles to calculate basic RSI
        // But prefer RSI_PERIOD * 2 + 1 for better accuracy with Wilder's smoothing
        if (recentCandles.size() < RSI_PERIOD + 1) {
            return null;
        }

        // Sort candles in chronological order (oldest first)
        recentCandles.sort(Comparator.comparing(CandleEntity::getCandleTimestamp));

        // Calculate initial average gain and loss for the first RSI_PERIOD
        double totalGain = 0.0;
        double totalLoss = 0.0;

        // Calculate gains and losses for the initial period
        for (int i = 1; i <= RSI_PERIOD; i++) {
            double priceChange = recentCandles.get(i).getClose() - recentCandles.get(i - 1).getClose();

            if (priceChange > 0) {
                totalGain += priceChange;
            } else if (priceChange < 0) {
                totalLoss += Math.abs(priceChange);
            }
        }

        // Initial averages (simple moving average for the first calculation)
        double avgGain = totalGain / RSI_PERIOD;
        double avgLoss = totalLoss / RSI_PERIOD;

        // Apply Wilder's smoothing for subsequent periods if we have more data
        for (int i = RSI_PERIOD + 1; i < recentCandles.size(); i++) {
            double priceChange = recentCandles.get(i).getClose() - recentCandles.get(i - 1).getClose();

            double currentGain = Math.max(priceChange, 0);
            double currentLoss = Math.abs(Math.min(priceChange, 0));

            // Wilder's smoothing: avgGain = ((avgGain * (period-1)) + currentGain) / period
            avgGain = ((avgGain * (RSI_PERIOD - 1)) + currentGain) / RSI_PERIOD;
            avgLoss = ((avgLoss * (RSI_PERIOD - 1)) + currentLoss) / RSI_PERIOD;
        }

        // Handle edge cases
        if (avgLoss == 0) {
            return avgGain == 0 ? 50.0 : 100.0; // If no gains either, RSI is neutral (50)
        }

        if (avgGain == 0) {
            return 0.0; // Only losses, RSI approaches 0
        }

        // Calculate Relative Strength (RS) and RSI
        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));

        // Ensure RSI stays within valid bounds (0-100)
        return Math.max(0.0, Math.min(100.0, rsi));
    }
}
