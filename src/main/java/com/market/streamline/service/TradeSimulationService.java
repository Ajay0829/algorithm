package com.market.streamline.service;

import com.market.streamline.dto.ChartTradeDTO;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.Trade;
import com.market.streamline.entity.Volatility;
import com.market.streamline.entity.Zone;
import com.market.streamline.kafka.ChartAnnotationProducer;
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
    private final ChartAnnotationProducer chartAnnotationProducer;

    public TradeSimulationService(TradeRepository tradeRepository, VolatilityRepository volatilityRepository, ZoneRepository zoneRepository, ChartAnnotationProducer chartAnnotationProducer) {
        this.tradeRepository = tradeRepository;
        this.volatilityRepository = volatilityRepository;
        this.zoneRepository = zoneRepository;
        this.chartAnnotationProducer = chartAnnotationProducer;
    }

    public void processActiveTrade(CandleEntity candleEntity, boolean isLow) {
        Optional<Trade> tradeOptional = tradeRepository.findFirstByStockSymbolAndTimeframeAndIsActiveTrue(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (tradeOptional.isPresent()) {
            Trade trade = tradeOptional.get();
            double currentHigh = candleEntity.getHigh();
            double currentLow = candleEntity.getLow();
            Zone zone = trade.getZone();

            if (!isLow) {
                if (trade.getTradeType().equals("BUY") && currentHigh >= trade.getTakeProfit()) {
                    trade.setResult("WIN");
                    trade.setIsActive(false);
                    zone.setNoOfTaps(zone.getNoOfTaps() + 1);
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);

                    ChartTradeDTO chartTradeDTO = getChartTradeDTO(candleEntity, trade, "updated");
                    chartTradeDTO = getResultingChartTradeDTO(candleEntity, trade, chartTradeDTO);
                    chartAnnotationProducer.sendAnnotation(
                            chartTradeDTO
                    );

                    System.out.println("TRADE RESULT: WIN - " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade closed at TP. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + trade.getEntryPrice() + ", Exit: " + trade.getTakeProfit() + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint());

                } else if (trade.getTradeType().equals("SELL") && currentHigh >= trade.getStopLoss()) {
                    trade.setResult("LOSS");
                    trade.setIsActive(false); // Set to false for stop loss hits
                    zone.setType("INVALID");
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);

                    ChartTradeDTO chartTradeDTO = getChartTradeDTO(candleEntity, trade, "updated");
                    chartTradeDTO = getResultingChartTradeDTO(candleEntity, trade, chartTradeDTO);
                    chartAnnotationProducer.sendAnnotation(
                            chartTradeDTO
                    );

                    System.out.println("TRADE RESULT: LOSS - " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade hit SL. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + trade.getEntryPrice() + ", Exit: " + trade.getStopLoss() + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint() + " marked INVALID");
                }
            } else {
                if (trade.getTradeType().equals("BUY") && currentLow <= trade.getStopLoss()) {
                    trade.setResult("LOSS");
                    trade.setIsActive(false); // Set to false for stop loss hits
                    zone.setType("INVALID");
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);

                    ChartTradeDTO chartTradeDTO = getChartTradeDTO(candleEntity, trade, "updated");
                    chartTradeDTO = getResultingChartTradeDTO(candleEntity, trade, chartTradeDTO);
                    chartAnnotationProducer.sendAnnotation(
                            chartTradeDTO
                    );

                    System.out.println("TRADE RESULT: LOSS - " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade hit SL. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + trade.getEntryPrice() + ", Exit: " + trade.getStopLoss() + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint() + " marked INVALID");

                } else if (trade.getTradeType().equals("SELL") && currentLow <= trade.getTakeProfit()) {
                    trade.setResult("WIN");
                    trade.setIsActive(false);
                    zone.setNoOfTaps(zone.getNoOfTaps() + 1);
                    zoneRepository.save(zone);
                    tradeRepository.save(trade);

                    ChartTradeDTO chartTradeDTO = getChartTradeDTO(candleEntity, trade, "updated");
                    chartTradeDTO = getResultingChartTradeDTO(candleEntity, trade, chartTradeDTO);
                    chartAnnotationProducer.sendAnnotation(
                            chartTradeDTO
                    );

                    System.out.println("TRADE RESULT: WIN - " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade closed at TP. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + trade.getEntryPrice() + ", Exit: " + trade.getTakeProfit() + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint());
                }
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
        ChartTradeDTO chartTradeDTO = getChartTradeDTO(candleEntity, trade, "executed");
        chartAnnotationProducer.sendAnnotation(chartTradeDTO);

        // Handle loss trades separately AFTER showing the entry
        if (isLossTrade) {
            trade.setResult("LOSS");
            trade.setIsActive(false);
            zone.setType("INVALID");
            tradeRepository.save(trade);
            zoneRepository.save(zone);

            // Send UPDATED event with exit details
            ChartTradeDTO exitTradeDTO = getChartTradeDTO(candleEntity, trade, "updated");
            exitTradeDTO = getResultingChartTradeDTO(candleEntity, trade, exitTradeDTO);
            chartAnnotationProducer.sendAnnotation(exitTradeDTO);

            System.out.println("AI LOSS TRADE " + candleEntity.getCandleTimestamp() + " " + trade.getTradeType());
        } else {
            System.out.println("TRADE TAKEN: " + trade.getTradeType() + " " + candleEntity.getTimeframe() + " trade opened. Symbol: " + candleEntity.getStockSymbol() + ", Entry: " + entryPrice + ", SL: " + stopLossPrice + ", TP: " + targetPrice + ", Zone: " + zone.getZoneType() + " " + zone.getNearPoint() + " (Strength: " + zone.getStrength() + ")");
        }
    }

    private ChartTradeDTO getResultingChartTradeDTO(CandleEntity candleEntity, Trade trade, ChartTradeDTO chartTradeDTO) {
        if (trade.getResult().equals("LOSS")) {
            chartTradeDTO.getData().setResult("LOSS");
            chartTradeDTO.getData().setIsActive(false);  // Add this line for consistency
            chartTradeDTO.getData().setExitPrice(trade.getStopLoss());
            chartTradeDTO.getData().setExitTimestamp(candleEntity.getCandleTimestamp().toString());
        } else {
            chartTradeDTO.getData().setResult("WIN");
            chartTradeDTO.getData().setIsActive(false);
            chartTradeDTO.getData().setExitPrice(trade.getTakeProfit());
            chartTradeDTO.getData().setExitTimestamp(candleEntity.getCandleTimestamp().toString());
        }
        return chartTradeDTO;
    }


    private ChartTradeDTO getChartTradeDTO(CandleEntity candleEntity, Trade trade, String action) {
        ChartTradeDTO.TradeData tradeData = new ChartTradeDTO.TradeData(
                trade.getId(),
                trade.getStockSymbol(),
                trade.getTimeframe(),
                trade.getTimestamp().toString(),
                trade.getEntryPrice(),
                trade.getStopLoss(),
                trade.getTakeProfit(),
                trade.getTradeType()
        );

        // Set additional fields needed for immediate plotting
        tradeData.setResult(trade.getResult() != null ? trade.getResult() : "PENDING");
        tradeData.setIsActive(trade.getIsActive());

        // For executed trades, set the entry details immediately
        if ("executed".equals(action)) {
            tradeData.setEntryTimestamp(trade.getTimestamp().toString());
            tradeData.setEntryPrice(trade.getEntryPrice());
        }

        return new ChartTradeDTO("trade", action, tradeData);
    }
}
