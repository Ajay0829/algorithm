package com.market.streamline.service;

import com.market.streamline.aggregation.CandleDataAggregationService;
import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.*;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class TradeSimulationService {

    private final TradeRepository tradeRepository;
    private final ZoneRepository zoneRepository;
    private final ChartAnnotationService chartAnnotationService;
    private final MarketIndicatorsRepository marketIndicatorsRepository;
    private final CandleRepository candleRepository;
    private final CandleDataAggregationService candleDataAggregationService;
    private final CandleAggregatedDataRepository candleAggregatedDataRepository;

    public TradeSimulationService(TradeRepository tradeRepository, ZoneRepository zoneRepository, ChartAnnotationService chartAnnotationService, MarketIndicatorsRepository marketIndicatorsRepository, CandleRepository candleRepository, CandleDataAggregationService candleDataAggregationService, CandleAggregatedDataRepository candleAggregatedDataRepository) {
        this.tradeRepository = tradeRepository;
        this.zoneRepository = zoneRepository;
        this.chartAnnotationService = chartAnnotationService;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
        this.candleRepository = candleRepository;
        this.candleDataAggregationService = candleDataAggregationService;
        this.candleAggregatedDataRepository = candleAggregatedDataRepository;
    }

    public void processTrades(CandleEntity candleEntity, boolean isHighCheck) {
        processActiveTrade(candleEntity, isHighCheck);
        processSweepTrade(candleEntity, isHighCheck);
    }

    public void processActiveTrade(CandleEntity candleEntity, boolean isHighCheck) {
        Optional<Trade> tradeOptional = tradeRepository.findFirstByStockSymbolAndTimeframeAndResultAndIsActiveTrue(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe(), "PENDING");
        if (tradeOptional.isPresent()) {
            Trade trade = tradeOptional.get();
            double currentHigh = candleEntity.getHigh();
            double currentLow = candleEntity.getLow();
            Zone zone = trade.getZone();

            if (isHighCheck) {
                if (trade.getTradeType().equals("BUY") && currentHigh >= trade.getTakeProfit()) {
                    evaluateTrade(trade, zone, candleEntity, "WIN");
                } else if (trade.getTradeType().equals("SELL") && currentHigh >= trade.getLossPoint()) {
                    evaluateTrade(trade, zone, candleEntity, "LOSS");
                } else if (trade.getTradeType().equals("SELL") && currentHigh >= trade.getStopLoss()) {
                    evaluateTrade(trade, zone, candleEntity, "SWEEP");
                }
            } else {
                if (trade.getTradeType().equals("BUY") && currentLow <= trade.getLossPoint()) {
                    evaluateTrade(trade, zone, candleEntity, "LOSS");
                } else if (trade.getTradeType().equals("BUY") && currentLow <= trade.getStopLoss()) {
                    evaluateTrade(trade, zone, candleEntity, "SWEEP");
                } else if (trade.getTradeType().equals("SELL") && currentLow <= trade.getTakeProfit()) {
                    evaluateTrade(trade, zone, candleEntity, "WIN");
                }
            }
        }
    }

    public void processSweepTrade(CandleEntity candleEntity, boolean isHighCheck) {
        Optional<Trade> tradeOptional = tradeRepository.findFirstByStockSymbolAndTimeframeAndResultAndIsActiveTrue(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe(), "SWEEP");
        if (tradeOptional.isPresent()) {
            Trade trade = tradeOptional.get();
            double currentHigh = candleEntity.getHigh();
            double currentLow = candleEntity.getLow();
            Zone zone = trade.getZone();

            if (isHighCheck) {
                if (trade.getTradeType().equals("BUY") && currentHigh >= trade.getTakeProfit()) {
                    trade.setIsActive(false);
                    evaluateTrade(trade, zone, candleEntity, "SWEEP");
                } else if (trade.getTradeType().equals("SELL") && currentHigh >= trade.getLossPoint()) {
                    evaluateTrade(trade, zone, candleEntity, "LOSS");
                }
            } else {
                if (trade.getTradeType().equals("BUY") && currentLow <= trade.getLossPoint()) {
                    evaluateTrade(trade, zone, candleEntity, "LOSS");
                } else if (trade.getTradeType().equals("SELL") && currentLow <= trade.getTakeProfit()) {
                    trade.setIsActive(false);
                    evaluateTrade(trade, zone, candleEntity, "SWEEP");
                }
            }
        }
    }

    // SL HIT, LOSS NOT HIT, TP HIT -> SWEEP
    // SL HIT, LOSS HIT -> LOSS, Active False
    // SL HIT, TP HIT -> WIN, Active False
    // SL HIT Active True
    public void evaluateTrade(Trade trade, Zone zone, CandleEntity candleEntity, String result) {
        trade.setResult(result);
        saveResultToCandleAggregatedData(trade, result);

        if (Objects.equals(result, "LOSS") || Objects.equals(result, "WIN")) {
            trade.setIsActive(false);
        }
        zone.setType(result.equals("WIN") ? "VALID" : "INVALID");
        zoneRepository.save(zone);
        tradeRepository.save(trade);

        if (zone.getType().equals("INVALID")) {
            chartAnnotationService.processZone(zone, "deleted");
        }
        chartAnnotationService.processTrade(trade, candleEntity, "updated");
    }

    public void saveResultToCandleAggregatedData(Trade trade, String result) {
        CandleAggregatedDataEntity candleAggregatedDataEntity = candleAggregatedDataRepository.findByStockSymbolAndTimeframeAndCandleTimestampAndTradeAndEntryPrice(
                trade.getStockSymbol(),
                trade.getTimeframe(),
                trade.getTimestamp(),
                trade.getTradeType(),
                trade.getEntryPrice()
        );
        candleAggregatedDataEntity.setTradeResult(result);
        candleAggregatedDataRepository.save(candleAggregatedDataEntity);
    }

    public void addTrade(CandleEntity candleEntity, Zone zone, double entryPrice) {
        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (marketIndicators == null) {
            return;
        }

        double volatilityValue = marketIndicators.getVolatility();
        double stopLossPrice, targetPrice, lossPoint;
        String zoneType = zone.getZoneType();

        if (zoneType.equals("DEMAND")) {
            stopLossPrice = entryPrice - entryPrice * 1*volatilityValue/100;
            lossPoint = stopLossPrice - entryPrice * 1.5*volatilityValue/100;
            targetPrice = entryPrice + entryPrice * 2.5*volatilityValue/100;
        } else {
            stopLossPrice = entryPrice + entryPrice * 1*volatilityValue/100;
            lossPoint = entryPrice + entryPrice * 1.5*volatilityValue/100;
            targetPrice = entryPrice - entryPrice * 2.5*volatilityValue/100;
        }

        long tradingHours = candleRepository.countByStockSymbolAndTimeframeAndCandleTimestampBetween(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                zone.getCandleTimestamp(),
                candleEntity.getCandleTimestamp()
        );

        Trade trade = new Trade(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp(),
                entryPrice,
                stopLossPrice,
                lossPoint,
                targetPrice,
                zone.getZoneType().equals("DEMAND") ? "BUY" : "SELL",
                true,
                tradingHours
        );

        zone.setType("ACTIVE");
        zone.setNoOfTaps(zone.getNoOfTaps() + 1);
        zoneRepository.save(zone);
        trade.setZone(zone);
        tradeRepository.save(trade);

        candleDataAggregationService.saveAggregatedData(candleEntity, trade);
        chartAnnotationService.processTrade(trade, candleEntity, "executed");
    }
}
