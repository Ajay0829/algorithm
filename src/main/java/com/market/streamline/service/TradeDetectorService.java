package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.Trend;
import com.market.streamline.entity.Volatility;
import com.market.streamline.entity.Zone;
import com.market.streamline.repository.TradeRepository;
import com.market.streamline.repository.TrendRepository;
import com.market.streamline.repository.VolatilityRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static java.lang.Math.abs;

@Service
public class TradeDetectorService {

    private final TrendRepository trendRepository;
    private final TradeSimulationService tradeSimulationService;
    private final ZoneRepository zoneRepository;
    private final TradeRepository tradeRepository;
    private final VolatilityRepository volatilityRepository;

    public TradeDetectorService(TrendRepository trendRepository, TradeSimulationService tradeSimulationService, ZoneRepository zoneRepository, TradeRepository tradeRepository, VolatilityRepository volatilityRepository) {
        this.trendRepository = trendRepository;
        this.tradeSimulationService = tradeSimulationService;
        this.zoneRepository = zoneRepository;
        this.tradeRepository = tradeRepository;
        this.volatilityRepository = volatilityRepository;
    }

    public void findTradeOpportunity(CandleEntity candleEntity, boolean isLow) {
        String stockSymbol = candleEntity.getStockSymbol();
        String timeframe = candleEntity.getTimeframe();
        LocalDateTime currentTimestamp = candleEntity.getCandleTimestamp();

        if (isLow) {
            // Processing low price - check for demand zones
            double lowPrice = candleEntity.getLow();
            // Check for active trade right before creating new trade
            boolean hasActiveTrade = tradeRepository.findFirstByStockSymbolAndTimeframeAndIsActiveTrue(
                    stockSymbol, timeframe).isPresent();
            List<Zone> validDemandZones = collectValidZones(stockSymbol, timeframe, "DEMAND", lowPrice, currentTimestamp, candleEntity, hasActiveTrade);
            Optional<Zone> demandZone = selectStrongestZone(validDemandZones);

            if (!hasActiveTrade) {
                demandZone.ifPresent(zone -> activateZoneAndAddTrade(zone, candleEntity));
            }
        } else {
            // Processing high price - check for supply zones
            double highPrice = candleEntity.getHigh();
            // Check for active trade right before creating new trade
            boolean hasActiveTrade = tradeRepository.findFirstByStockSymbolAndTimeframeAndIsActiveTrue(
                    stockSymbol, timeframe).isPresent();
            List<Zone> validSupplyZones = collectValidZones(stockSymbol, timeframe, "SUPPLY", highPrice, currentTimestamp, candleEntity, hasActiveTrade);
            Optional<Zone> supplyZone = selectStrongestZone(validSupplyZones);

            if (!hasActiveTrade) {
                supplyZone.ifPresent(zone -> activateZoneAndAddTrade(zone, candleEntity));
            }
        }
    }

    private List<Zone> collectValidZones(String stockSymbol, String timeframe, String zoneType, double currentPrice, LocalDateTime currentTimestamp, CandleEntity candleEntity, boolean hasActiveTrade) {
        List<Zone> validZones = new ArrayList<>();

        // Add crossed zones that haven't been invalidated
        validZones.addAll(getValidCrossedZones(stockSymbol, timeframe, zoneType, currentPrice, currentTimestamp, candleEntity, hasActiveTrade));

        // Add nearby unbreached zones
        addNearbyUnbreachedZone(stockSymbol, timeframe, zoneType, currentPrice, currentTimestamp, validZones);

        return validZones;
    }

    private List<Zone> getValidCrossedZones(String stockSymbol, String timeframe, String zoneType, double currentPrice, LocalDateTime currentTimestamp, CandleEntity candleEntity, boolean hasActiveTrade) {
        List<Zone> crossedZones = getCrossedZonesByType(stockSymbol, timeframe, zoneType, currentPrice, currentTimestamp);

        List<Zone> validZones = new ArrayList<>();
        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (volatility == null) {
            return Collections.emptyList();
        }
        double invalidationThreshold = volatility.getVolatility();

        for (Zone zone : crossedZones) {
            if (isZoneInvalidated(zone, zoneType, currentPrice, invalidationThreshold)) {
                zone.setType("INVALID");
                zoneRepository.save(zone);
                if (!hasActiveTrade) {
                    tradeSimulationService.addTrade(candleEntity, zone, true);
                }
            } else {
                validZones.add(zone);
            }
        }

        return validZones;
    }

    private List<Zone> getCrossedZonesByType(String stockSymbol, String timeframe, String zoneType, double currentPrice, LocalDateTime currentTimestamp) {
        if ("DEMAND".equals(zoneType)) {
            return zoneRepository.findDemandZonesWherePriceBelowNearPoint(stockSymbol, timeframe, currentPrice, currentTimestamp);
        } else {
            return zoneRepository.findSupplyZonesWherePriceAboveNearPoint(stockSymbol, timeframe, currentPrice, currentTimestamp);
        }
    }

    private boolean isZoneInvalidated(Zone zone, String zoneType, double currentPrice, double thresholdPercent) {
        double farPoint = zone.getFarPoint();

        if ("DEMAND".equals(zoneType)) {
            // For demand: invalidated when price goes below farPoint - threshold
            double thresholdPrice = farPoint - (farPoint * thresholdPercent / 100);
            return currentPrice < thresholdPrice;
        } else {
            // For supply: invalidated when price goes above farPoint + threshold
            double thresholdPrice = farPoint + (farPoint * thresholdPercent / 100);
            return currentPrice > thresholdPrice;
        }
    }

    private void addNearbyUnbreachedZone(String stockSymbol, String timeframe, String zoneType, double currentPrice, LocalDateTime currentTimestamp, List<Zone> validZones) {
        Optional<Zone> unbreachedZone = getUnbreachedZoneByType(stockSymbol, timeframe, zoneType, currentPrice, currentTimestamp);

        if (unbreachedZone.isPresent()) {
            Zone zone = unbreachedZone.get();
            if (isZoneNearby(zone, zoneType, currentPrice, 0.1)) {
                validZones.add(zone);
            }
        }
    }

    private Optional<Zone> getUnbreachedZoneByType(String stockSymbol, String timeframe, String zoneType, double currentPrice, LocalDateTime currentTimestamp) {
        if ("DEMAND".equals(zoneType)) {
            return zoneRepository.findClosestDemandZoneWherePriceAboveOrEqualNearPoint(stockSymbol, timeframe, currentPrice, currentTimestamp);
        } else {
            return zoneRepository.findClosestSupplyZoneWherePriceBelowOrEqualNearPoint(stockSymbol, timeframe, currentPrice, currentTimestamp);
        }
    }

    private boolean isZoneNearby(Zone zone, String zoneType, double currentPrice, double proximityThresholdPercent) {
        double nearPoint = zone.getNearPoint();

        if ("DEMAND".equals(zoneType)) {
            // For demand: check if price is within threshold above nearPoint
            double proximityPrice = nearPoint + (nearPoint * proximityThresholdPercent / 100);
            return currentPrice <= proximityPrice && currentPrice >= nearPoint;
        } else {
            // For supply: check if price is within threshold below nearPoint
            double proximityPrice = nearPoint - (nearPoint * proximityThresholdPercent / 100);
            return currentPrice >= proximityPrice && currentPrice <= nearPoint;
        }
    }

    private Optional<Zone> selectStrongestZone(List<Zone> validZones) {
        if (validZones.isEmpty()) {
            return Optional.empty();
        }

        Zone strongestZone = findStrongestZone(validZones);
        markWeakerZones(validZones, strongestZone);
        return Optional.of(strongestZone);
    }

    private Zone findStrongestZone(List<Zone> zones) {
        return zones.stream()
                .max((z1, z2) -> {
                    double strength1 = z1.getStrength() != null ? z1.getStrength() : 0.0;
                    double strength2 = z2.getStrength() != null ? z2.getStrength() : 0.0;
                    return Double.compare(strength1, strength2);
                })
                .orElse(zones.get(0));
    }

    private void markWeakerZones(List<Zone> zones, Zone strongestZone) {
        zones.stream()
                .filter(zone -> !zone.equals(strongestZone))
                .forEach(zone -> {
                    zone.setType("INVALID");
                    zoneRepository.save(zone);
                });
    }

    private void activateZoneAndAddTrade(Zone strongestZone, CandleEntity candleEntity) {
        strongestZone.setType("ACTIVE");
        zoneRepository.save(strongestZone);
        tradeSimulationService.addTrade(candleEntity, strongestZone, false);
    }
}
