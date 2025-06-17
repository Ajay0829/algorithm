package com.market.streamline.service;

import com.market.streamline.entity.StockFeature;
import com.market.streamline.repository.StockFeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class FeatureExtractionService {

    @Autowired
    private StockFeatureRepository stockFeatureRepository;

    // This method should be called when a new candle event is received
    @Transactional
    public void processCandleEvent(Map<String, Object> event) {
        // Optional<StockFeature> existing = stockFeatureRepository.findByStockSymbolAndTimeframeAndCandleTimestamp(stockSymbol, timeframe, candleTimestamp);
        // Ideally stockSymbol, timeframe, and candleTimestamp should be unique together

        // Based on time frame, stock symbol need to fetch the existing data ( swing points, trend, zones etc.)

        // need tables to store swing points, trend, zones, fundamentals, BOS, Liquidity sweeps etc. per stock symbol and timeframe

        // save to repository
        // stockFeatureRepository.save(feature);
    }
}

