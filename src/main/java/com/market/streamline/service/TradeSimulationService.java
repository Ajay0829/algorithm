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

    public void processActiveTrade(CandleEntity candleEntity, boolean isLow) {
        Optional<Trade> tradeOptional = tradeRepository.findFirstByStockSymbolAndTimeframeAndIsActiveTrue(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (tradeOptional.isPresent()) {
            Trade trade = tradeOptional.get();
            double currentHigh = candleEntity.getHigh();
            double currentLow = candleEntity.getLow();
            Zone zone = trade.getZone();
            boolean isTradeDone = false;

            if (!isLow) {
                if (trade.getTradeType().equals("BUY") && currentHigh >= trade.getTakeProfit()) {
                    trade.setResult("WIN");
                    trade.setIsActive(false);
                    zone.setNoOfTaps(zone.getNoOfTaps() + 1);
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);
                    isTradeDone = true;
                } else if (trade.getTradeType().equals("SELL") && currentHigh >= trade.getStopLoss()) {
                    trade.setResult("LOSS");
                    trade.setIsActive(false); // Set to false for stop loss hits
                    zone.setType("INVALID");
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);
                    isTradeDone = true;
                }
            } else {
                if (trade.getTradeType().equals("BUY") && currentLow <= trade.getStopLoss()) {
                    trade.setResult("LOSS");
                    trade.setIsActive(false); // Set to false for stop loss hits
                    zone.setType("INVALID");
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);
                    isTradeDone = true;

                } else if (trade.getTradeType().equals("SELL") && currentLow <= trade.getTakeProfit()) {
                    trade.setResult("WIN");
                    trade.setIsActive(false);
                    zone.setNoOfTaps(zone.getNoOfTaps() + 1);
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);
                    isTradeDone = true;
                }
            }

            if (isTradeDone) {
                chartAnnotationService.processTrade(trade, candleEntity, "updated");
            }
        }
    }

    public void addTrade(CandleEntity candleEntity, Zone zone, boolean isLossTrade) {
        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());

        if (volatility == null) {
            // Handle case where volatility data is not available
            return;
        }

        double volatilityValue = volatility.getVolatility();
        double entryPrice, stopLossPrice, targetPrice;
        String zoneType = zone.getZoneType();

        boolean isGreenCandle = candleEntity.getOpen() < candleEntity.getClose();

        if (zoneType.equals("DEMAND")) {
            if (isGreenCandle) {
                entryPrice = Math.min(zone.getNearPoint(), candleEntity.getOpen());
            } else {
                entryPrice = zone.getNearPoint();
            }
            stopLossPrice = entryPrice - entryPrice * 2*volatilityValue/100;
            targetPrice = entryPrice + entryPrice * 5*volatilityValue/100;
        } else {
            if (!isGreenCandle) {
                entryPrice = Math.max(zone.getNearPoint(), candleEntity.getOpen());
            } else {
                entryPrice = zone.getNearPoint();
            }
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
                true // Always start as active
        );

        // Set the zone and save the trade FIRST to get proper ID
        trade.setZone(zone);
        tradeRepository.save(trade);

        // Send EXECUTED event immediately for ALL trades (show entry marker right away)
        chartAnnotationService.processTrade(trade, candleEntity, "executed");

        // Handle loss trades separately AFTER showing the entry
        if (isLossTrade) {
            trade.setResult("LOSS");
            trade.setIsActive(false);
            zone.setType("INVALID");
            tradeRepository.save(trade);
            zoneRepository.save(zone);

            chartAnnotationService.processTrade(trade, candleEntity, "updated");

//            System.out.println("AI LOSS TRADE " + candleEntity.getCandleTimestamp() + " " + trade.getTradeType());
        } else {
//            System.out.println("TRADE TAKEN: " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade opened. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + entryPrice + ", SL: " + stopLossPrice + ", TP: " + targetPrice + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint() + " (Strength: " + zone.getStrength() + ")");
        }
    }
}
