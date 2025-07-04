package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.MarketIndicatorsRepository;
import com.market.streamline.repository.TradeRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TradeSimulationService {

    private final TradeRepository tradeRepository;
    private final ZoneRepository zoneRepository;
    private final ChartAnnotationService chartAnnotationService;
    private final MarketIndicatorsRepository marketIndicatorsRepository;

    public TradeSimulationService(TradeRepository tradeRepository, ZoneRepository zoneRepository, ChartAnnotationService chartAnnotationService, MarketIndicatorsRepository marketIndicatorsRepository) {
        this.tradeRepository = tradeRepository;
        this.zoneRepository = zoneRepository;
        this.chartAnnotationService = chartAnnotationService;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
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
        chartAnnotationService.processTrade(trade, candleEntity, "updated");
    }

    public void addTrade(CandleEntity candleEntity, Zone zone, double entryPrice) {
        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (marketIndicators == null) {
            return;
        }

        double volatilityValue = marketIndicators.getVolatility();
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
    }
}
