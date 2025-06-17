package com.market.analysis.technical.service;

import com.market.external.polygon.dto.Candle;
import java.util.*;

public class RSIService {
    public List<Double> calculateRSI(List<Candle> candles, int period) {
        List<Double> rsi = new ArrayList<>();
        if (candles == null || candles.size() < period + 1) {
            for (int i = 0; i < candles.size(); i++) rsi.add(null);
            return rsi;
        }
        double gain = 0.0, loss = 0.0;
        for (int i = 1; i <= period; i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            if (change > 0) gain += change;
            else loss -= change;
        }
        gain /= period;
        loss /= period;
        double rs = loss == 0 ? 100 : gain / loss;
        rsi.add(null); // first value is always null
        for (int i = 1; i < period; i++) rsi.add(null);
        rsi.add(100 - (100 / (1 + rs)));
        for (int i = period + 1; i < candles.size(); i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            if (change > 0) {
                gain = (gain * (period - 1) + change) / period;
                loss = (loss * (period - 1)) / period;
            } else {
                gain = (gain * (period - 1)) / period;
                loss = (loss * (period - 1) - change) / period;
            }
            rs = loss == 0 ? 100 : gain / loss;
            rsi.add(100 - (100 / (1 + rs)));
        }
        return rsi;
    }
}

