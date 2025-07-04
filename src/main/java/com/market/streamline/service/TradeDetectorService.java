package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.repository.MarketIndicatorsRepository;
import com.market.streamline.repository.TradeRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TradeDetectorService {

    private final TradeSimulationService tradeSimulationService;
    private final ZoneRepository zoneRepository;
    private final TradeRepository tradeRepository;
    private final MarketIndicatorsRepository marketIndicatorsRepository;

    public TradeDetectorService(TradeSimulationService tradeSimulationService, ZoneRepository zoneRepository, TradeRepository tradeRepository, MarketIndicatorsRepository marketIndicatorsRepository) {
        this.tradeSimulationService = tradeSimulationService;
        this.zoneRepository = zoneRepository;
        this.tradeRepository = tradeRepository;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
    }

    public void findTradeOpportunity(CandleEntity candleEntity, boolean isHighCheck) {

        String stockSymbol = candleEntity.getStockSymbol();
        String timeframe = candleEntity.getTimeframe();

        if (tradeRepository.existsByStockSymbolAndTimeframeAndIsActiveTrue(stockSymbol, timeframe)) {
            return; // Skip if there's already an active trade
        }

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (marketIndicators == null) {
            return;
        }

        double volatilityValue = marketIndicators.getVolatility();

        double currentPrice = isHighCheck ? candleEntity.getHigh() : candleEntity.getLow();

        Optional<Zone> mayBeDemandZone = zoneRepository.findLatestZoneByType(stockSymbol, timeframe, "DEMAND", candleEntity.getCandleTimestamp());
        Optional<Zone> mayBeSupplyZone = zoneRepository.findLatestZoneByType(stockSymbol, timeframe, "SUPPLY", candleEntity.getCandleTimestamp());

        // TODO: Decide proximity threshold to decide the trade
        // Check demand zones
        if (mayBeDemandZone.isPresent()) {
            Zone demandZone = mayBeDemandZone.get();
            if (isZoneNearby(demandZone, currentPrice, volatilityValue/5)) {
                activateZoneAndAddTrade(demandZone, candleEntity, calculateEntryPrice(candleEntity, demandZone));
                return; // Exit after finding a trade opportunity
            }
        }

        // Check supply zones independently
        if (mayBeSupplyZone.isPresent()) {
            Zone supplyZone = mayBeSupplyZone.get();
            if (isZoneNearby(supplyZone, currentPrice, volatilityValue/5)) {
                activateZoneAndAddTrade(supplyZone, candleEntity, calculateEntryPrice(candleEntity, supplyZone));
            }
        }
    }

    private double calculateEntryPrice(CandleEntity candleEntity, Zone zone) {
        double entryPrice = candleEntity.getOpen();
        if ("DEMAND".equals(zone.getZoneType())) {
            if (entryPrice >= zone.getFarPoint() && entryPrice <= zone.getNearPoint()) {
                return entryPrice;
            } else {
                return zone.getNearPoint();
            }
        } else {
            if (entryPrice <= zone.getFarPoint() && entryPrice >= zone.getNearPoint()) {
                return entryPrice;
            } else {
                return zone.getNearPoint();
            }
        }
    }

    private boolean isZoneNearby(Zone zone, double currentPrice, double proximityThresholdPercent) {
        double nearPoint = zone.getNearPoint();

        if ("DEMAND".equals(zone.getZoneType())) {
            double proximityPrice = nearPoint + nearPoint * proximityThresholdPercent / 100;
            return currentPrice <= proximityPrice;
        } else {
            double proximityPrice = nearPoint - nearPoint * proximityThresholdPercent / 100;
            return currentPrice >= proximityPrice;
        }
    }

    private void activateZoneAndAddTrade(Zone zone, CandleEntity candleEntity, double price) {
        tradeSimulationService.addTrade(candleEntity, zone, price);
    }
}
