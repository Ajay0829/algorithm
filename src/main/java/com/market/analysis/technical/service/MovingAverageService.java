package com.market.analysis.technical.service;

import com.market.external.polygon.dto.Candle;
import java.util.*;

public class MovingAverageService {
    // Simple Moving Average
    public List<Double> calculateSMA(List<Candle> candles, int period) {
        List<Double> sma = new ArrayList<>();
        double sum = 0.0;
        for (int i = 0; i < candles.size(); i++) {
            double close = candles.get(i).getClose();
            sum += close;
            if (i >= period) {
                sum -= candles.get(i - period).getClose();
            }
            if (i >= period - 1) {
                sma.add(sum / period);
            } else {
                sma.add(null);
            }
        }
        return sma;
    }
}

