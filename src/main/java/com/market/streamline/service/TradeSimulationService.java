package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.Trade;
import com.market.streamline.entity.Zone;
import com.market.streamline.repository.TradeRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class TradeSimulationService {

    private final TradeRepository tradeRepository;

    public TradeSimulationService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void processActiveTrades(CandleEntity candleEntity) {
        // Max one trade per stock symbol and timeframe
        // Get the current status of the trade and once trade is completed, add the result to the trade history.
    }

    public void addTrade(CandleEntity candleEntity, Zone zone, boolean isOppositeZoneFormed) {
        // Logic to add a trade based on the candle entity and zone.
        // This method will be called from TradeDetectorService.
        double entryPrice, stopLossPrice, targetPrice;
        Pair<Double, Double> stopLossAndTarget = getStopLossAndTarget(zone, isOppositeZoneFormed);
        if (isOppositeZoneFormed) {
            entryPrice = zone.getFarPoint();
            stopLossPrice = stopLossAndTarget.getFirst();
            targetPrice = stopLossAndTarget.getSecond();
        } else {
            entryPrice = zone.getNearPoint();
            stopLossPrice = stopLossAndTarget.getFirst();
            targetPrice = stopLossAndTarget.getSecond();
        }

        Trade trade = new Trade(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp(),
                entryPrice,
                stopLossPrice,
                targetPrice,
                true
        );
        tradeRepository.save(trade);
    }

    Pair<Double, Double> getStopLossAndTarget(Zone zone, boolean isOppositeZoneFormed) {
        double stopLossPrice, targetPrice;
        String zoneType = zone.getType();
        if (zoneType.equals("DEMAND")) {
            if (isOppositeZoneFormed) {
                stopLossPrice = zone.getFarPoint() * 0.95;
                targetPrice = zone.getFarPoint() * 1.10;
            } else {
                stopLossPrice = zone.getFarPoint() * 0.98;
                targetPrice = zone.getNearPoint() * 1.10;
            }
        } else {
            if (isOppositeZoneFormed) {
                stopLossPrice = zone.getFarPoint() * 1.05;
                targetPrice = zone.getNearPoint() * 0.90;
            } else {
                stopLossPrice = zone.getFarPoint() * 1.02;
                targetPrice = zone.getFarPoint() * 0.90;
            }
        }

        return Pair.of(stopLossPrice, targetPrice);
    }
}
