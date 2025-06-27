package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.Trend;
import com.market.streamline.entity.Zone;
import com.market.streamline.repository.TrendRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.abs;

@Service
public class TradeDetectorService {

    private final TrendRepository trendRepository;
    private final TradeSimulationService tradeSimulationService;
    private final ZoneRepository zoneRepository;

    public TradeDetectorService(TrendRepository trendRepository, TradeSimulationService tradeSimulationService, ZoneRepository zoneRepository) {
        this.trendRepository = trendRepository;
        this.tradeSimulationService = tradeSimulationService;
        this.zoneRepository = zoneRepository;
    }

    /*
     Fetch the current trend for the stock symbol and timeframe
     Check if valid zone according to the trend exists?
     Check if the candle entity is roughly near the zone (within a certain threshold)
     Check if there was an opposite zone formed before retest,
     if so, take entry at far point, otherwise take entry at near point.
     Add the trade to the topic for execution.
     */
    public void detectTradeOpportunity(CandleEntity candleEntity) {
        Trend trend = trendRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe()
        );
        if (trend == null) { return; }

        Optional<Zone> zone = fetchValidZone(trend, candleEntity);
        if (zone.isEmpty()) { return; }

        LocalDateTime zoneIdentificationTime = zone.get().getIdentifiedAt();

        if (candleEntity.getCandleTimestamp().isAfter(zoneIdentificationTime)) {
            boolean isOppositeZoneFormed = checkForOppositeZoneFormation(candleEntity, zone.get());
            boolean isPriceNearZone = isPriceNearZone(candleEntity, zone.get(), isOppositeZoneFormed);

            if (isPriceNearZone) {
                int noOfTaps = zone.get().getNoOfTaps() == null ? 0 : zone.get().getNoOfTaps();
                // send trade event
                if (noOfTaps == 0) {
                    zone.get().setNoOfTaps(1);
//                    System.out.println("=======>>>> Tapped zone: " + zone.get().getCandleTimestamp() + " at: " + candleEntity.getCandleTimestamp());
//                    System.out.println("Zone Type: " + zone.get().getZoneType() + " near Point: " + zone.get().getNearPoint() + " far Point: " + zone.get().getFarPoint());
                    tradeSimulationService.addTrade(candleEntity, zone.get(), isOppositeZoneFormed);
                } else {
                    zone.get().setNoOfTaps(noOfTaps + 1);
                }
                zoneRepository.save(zone.get());
            }
        }

    }

    boolean isPriceNearZone(CandleEntity candleEntity, Zone zone, boolean isOppositeZoneFormed) {
        double checkPrice = isOppositeZoneFormed ? zone.getFarPoint() : zone.getNearPoint();
        double currentPrice = Objects.equals(zone.getZoneType(), "DEMAND") ? candleEntity.getLow() : candleEntity.getHigh();
        double percentageThreshold = 0.2;
        boolean noTouchCondition = abs(currentPrice - checkPrice) * 100 / checkPrice <= percentageThreshold;
        if (zone.getZoneType().equals("DEMAND")) {
            if (currentPrice > checkPrice) {
                return noTouchCondition;
            }
        } else {
            if (currentPrice < checkPrice) {
                return noTouchCondition;
            }
        }
        return true;
    }

    boolean checkForOppositeZoneFormation(CandleEntity candleEntity, Zone zone) {
        String zoneType = zone.getZoneType();
        Zone recentZone = zoneRepository. findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp()
        );
        if (recentZone == null) { return false; }
        if (recentZone.getZoneType().equals(zoneType)) { return false; }
        else {
            return recentZone.getCandleTimestamp().isAfter(zone.getCandleTimestamp());
        }
    }

    Optional<Zone> fetchValidZone(Trend trend, CandleEntity candleEntity) {
        // Logic to fetch a valid zone based on the trend and candle entity.
        LocalDateTime candleTimestamp = candleEntity.getCandleTimestamp();
        if (trend.getType().equals("SIDEWAYS")) {
            // If the trend is sideways, we don't consider zones for trading.
            return Optional.empty();
        }
        Zone zone = zoneRepository.findTopByStockSymbolAndTimeframeAndZoneTypeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                Objects.equals(trend.getType(), "UP") ? "DEMAND": "SUPPLY",
                candleTimestamp
        );
        if (zone == null) {return Optional.empty();}
        return Optional.of(zone);
    }
}
