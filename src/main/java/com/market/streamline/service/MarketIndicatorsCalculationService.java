package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;

@Service
public class MarketIndicatorsCalculationService {

    private final MarketIndicatorsRepository marketIndicatorsRepository;
    private final CandleRepository candleRepository;
    private static final int RSI_PERIOD = 14;
    private static final int MOVING_AVERAGE_WINDOW = 200;

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
                    candleEntity.getVolume(),
                    null, // RSI will be calculated below
                    1
            );
        } else {
            // Update average volatility
            double currentVolatility = marketIndicators.getAverageVolatility();
            CandleEntity previousCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
                    candleEntity.getStockSymbol(), candleEntity.getTimeframe(), candleEntity.getCandleTimestamp()
            );
            double previousClose = previousCandle.getClose();
            double atr = getATR(previousClose, candleEntity) * 100;

            int totalSamples = marketIndicators.getNoOfSamples();
            double finalVolatility = (currentVolatility * totalSamples + atr) / (totalSamples + 1);
            marketIndicators.setAverageVolatility(finalVolatility);

            // Update average volume
            if (marketIndicators.getAverageVolume() != null) {
                double currentAvgVolume = marketIndicators.getAverageVolume();
                double finalAvgVolume = (currentAvgVolume * totalSamples + candleEntity.getVolume()) / (totalSamples + 1);
                marketIndicators.setAverageVolume(finalAvgVolume);
            } else {
                marketIndicators.setAverageVolume(candleEntity.getVolume());
            }

            marketIndicators.setNoOfSamples(min(totalSamples + 1, MOVING_AVERAGE_WINDOW - 1));
        }

        // Calculate and update RSI
        Double rsi = calculateRSI(stockSymbol, timeframe, candleEntity);
        if (rsi != null) {
            marketIndicators.setRsi14(rsi);
        }

        marketIndicatorsRepository.save(marketIndicators);
    }

    double getATR(double previousClose, CandleEntity candleEntity) {
        double volatility = max(abs(candleEntity.getHigh() - candleEntity.getLow()), abs(candleEntity.getHigh() - previousClose));
        volatility = max(volatility, abs(candleEntity.getLow() - previousClose));
        volatility /= candleEntity.getClose();

        return volatility;
    }

    /**
     * Calculate RSI (Relative Strength Index) using Wilder's smoothing method (standard in trading platforms)
     */
    private Double calculateRSI(String stockSymbol, String timeframe, CandleEntity currentCandle) {
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
