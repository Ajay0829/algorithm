package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.Trade;
import com.market.streamline.entity.Volatility;
import com.market.streamline.entity.Zone;
import com.market.streamline.repository.TradeRepository;
import com.market.streamline.repository.VolatilityRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TradeSimulationService {

    private final TradeRepository tradeRepository;
    private final VolatilityRepository volatilityRepository;
    private final ZoneRepository zoneRepository;
    private final ChartAnnotationService chartAnnotationService;

    public TradeSimulationService(TradeRepository tradeRepository, VolatilityRepository volatilityRepository, ZoneRepository zoneRepository, ChartAnnotationService chartAnnotationService) {
        this.tradeRepository = tradeRepository;
        this.volatilityRepository = volatilityRepository;
        this.zoneRepository = zoneRepository;
        this.chartAnnotationService = chartAnnotationService;
    }

    public void processActiveTrade(CandleEntity candleEntity, boolean isHighCheck) {
        Optional<Trade> tradeOptional = tradeRepository.findFirstByStockSymbolAndTimeframeAndIsActiveTrue(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (tradeOptional.isPresent()) {
            Trade trade = tradeOptional.get();
            double currentHigh = candleEntity.getHigh();
            double currentLow = candleEntity.getLow();
            Zone zone = trade.getZone();

            if (isHighCheck) {
                if (trade.getTradeType().equals("BUY") && currentHigh >= trade.getTakeProfit()) {
                    evaluateTrade(trade, zone, candleEntity, "WIN");
                } else if (trade.getTradeType().equals("SELL") && currentHigh >= trade.getStopLoss()) {
                    evaluateTrade(trade, zone, candleEntity, "LOSS");
                }
            } else {
                if (trade.getTradeType().equals("BUY") && currentLow <= trade.getStopLoss()) {
                    evaluateTrade(trade, zone, candleEntity, "LOSS");

                } else if (trade.getTradeType().equals("SELL") && currentLow <= trade.getTakeProfit()) {
                    evaluateTrade(trade, zone, candleEntity, "WIN");
                }
            }
        }
    }

    public void evaluateTrade(Trade trade, Zone zone, CandleEntity candleEntity, String result) {
        trade.setResult(result);
        trade.setIsActive(false);
        zone.setType(result.equals("WIN") ? "VALID" : "INVALID");
        zoneRepository.save(zone);
        tradeRepository.save(trade);
        if (zone.getType().equals("INVALID")) {
            chartAnnotationService.processZone(zone, "deleted");
        }
        System.out.println("Trade Closed: " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade closed. Symbol: " + candleEntity.getStockSymbol() + ", Result: " + trade.getResult() + ", Entry: " + trade.getEntryPrice() + ", SL: " + trade.getStopLoss() + ", TP: " + trade.getTakeProfit() + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint() + " (Strength: " + zone.getStrength() + ")");
        chartAnnotationService.processTrade(trade, candleEntity, "updated");
    }

    public void addTrade(CandleEntity candleEntity, Zone zone, double entryPrice) {
        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());

        if (volatility == null) {
            return;
        }

        double volatilityValue = volatility.getVolatility();
        double stopLossPrice, targetPrice;
        String zoneType = zone.getZoneType();

        if (zoneType.equals("DEMAND")) {
            stopLossPrice = entryPrice - entryPrice * 2*volatilityValue/100;
            targetPrice = entryPrice + entryPrice * 5*volatilityValue/100;
        } else {
            stopLossPrice = entryPrice + entryPrice * 2*volatilityValue/100;
            targetPrice = entryPrice - entryPrice * 5*volatilityValue/100;
        }

        Trade trade = new Trade(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp(),
                entryPrice,
                stopLossPrice,
                targetPrice,
                zone.getZoneType().equals("DEMAND") ? "BUY" : "SELL",
                true
        );

        zone.setType("ACTIVE");
        zone.setNoOfTaps(zone.getNoOfTaps() + 1);
        zoneRepository.save(zone);
        trade.setZone(zone);
        tradeRepository.save(trade);

        chartAnnotationService.processTrade(trade, candleEntity, "executed");

        System.out.println("TRADE TAKEN: " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade opened. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + entryPrice + ", SL: " + stopLossPrice + ", TP: " + targetPrice + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint() + " (Strength: " + zone.getStrength() + ")");
    }
}
