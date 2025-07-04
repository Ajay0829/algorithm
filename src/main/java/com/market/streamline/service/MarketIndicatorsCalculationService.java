package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Math.abs;

@Service
public class MarketIndicatorsCalculationService {

    private final MarketIndicatorsRepository marketIndicatorsRepository;
    private final CandleRepository candleRepository;
    private static final int RSI_PERIOD = 14;

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
            double initialVolatility = abs(candleEntity.getOpen() - candleEntity.getClose()) * 100 / candleEntity.getOpen();
            marketIndicators = new MarketIndicators(
                    stockSymbol,
                    timeframe,
                    initialVolatility,
                    candleEntity.getVolume(),
                    null // RSI will be calculated below
            );
        } else {
            // Update average volatility
            double currentVolatility = marketIndicators.getAverageVolatility();
            double currentMove = abs(candleEntity.getOpen() - candleEntity.getClose()) * 100 / candleEntity.getOpen();
            long totalCandles = candleRepository.countByStockSymbolAndTimeframe(stockSymbol, timeframe);
            double finalVolatility = (currentVolatility * (totalCandles - 1) + currentMove) / totalCandles;
            marketIndicators.setAverageVolatility(finalVolatility);

            // Update average volume
            if (marketIndicators.getAverageVolume() != null) {
                double currentAvgVolume = marketIndicators.getAverageVolume();
                double finalAvgVolume = (currentAvgVolume * (totalCandles - 1) + candleEntity.getVolume()) / totalCandles;
                marketIndicators.setAverageVolume(finalAvgVolume);
            } else {
                marketIndicators.setAverageVolume(candleEntity.getVolume());
            }
        }

        // Calculate and update RSI
        Double rsi = calculateRSI(stockSymbol, timeframe, candleEntity);
        if (rsi != null) {
            marketIndicators.setRsi14(rsi);
        }

        marketIndicatorsRepository.save(marketIndicators);
    }

    /**
     * Calculate RSI (Relative Strength Index) using the standard 14-period calculation
     */
    private Double calculateRSI(String stockSymbol, String timeframe, CandleEntity currentCandle) {
        // Get the most recent 15 candles (including current one) to calculate 14-period RSI
        List<CandleEntity> recentCandles = candleRepository.findRecentCandles(
                stockSymbol,
                timeframe,
                currentCandle.getCandleTimestamp(),
                PageRequest.of(0, RSI_PERIOD + 1)
        );

        // Need at least RSI_PERIOD + 1 candles to calculate RSI
        if (recentCandles.size() < RSI_PERIOD + 1) {
            return null;
        }

        double totalGain = 0.0;
        double totalLoss = 0.0;

        // Calculate gains and losses over the RSI period
        for (int i = 0; i < RSI_PERIOD; i++) {
            CandleEntity currentPeriodCandle = recentCandles.get(i);
            CandleEntity previousCandle = recentCandles.get(i + 1);

            double priceChange = currentPeriodCandle.getClose() - previousCandle.getClose();

            if (priceChange > 0) {
                totalGain += priceChange;
            } else if (priceChange < 0) {
                totalLoss += Math.abs(priceChange);
            }
        }

        // Calculate average gain and average loss
        double avgGain = totalGain / RSI_PERIOD;
        double avgLoss = totalLoss / RSI_PERIOD;

        // Avoid division by zero
        if (avgLoss == 0) {
            return 100.0; // RSI = 100 when there are no losses
        }

        // Calculate Relative Strength (RS) and RSI
        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));

        return rsi;
    }

    // Backward compatibility method for existing volatility usage
    public Double getVolatility(String stockSymbol, String timeframe) {
        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        return marketIndicators != null ? marketIndicators.getAverageVolatility() : null;
    }
}
