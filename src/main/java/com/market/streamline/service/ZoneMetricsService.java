package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneMetricsService {
    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;
    private final MarketIndicatorsRepository marketIndicatorsRepository;

    public ZoneMetricsService(ZoneRepository zoneRepository,
                              CandleRepository candleRepository,
                              MarketIndicatorsRepository marketIndicatorsRepository) {
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
    }

    public void updateZoneMetrics(CandleEntity candleEntity) {
        List<Zone> zones = zoneRepository.findZonesWithMissingMetrics(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe());

        for (Zone zone : zones) {
            long candles = candleRepository.countCandlesBetweenTimestamps(
                    zone.getStockSymbol(),
                    zone.getTimeframe(),
                    zone.getIdentifiedAt(),
                    candleEntity.getCandleTimestamp());

            if (candles <= 0) continue;

            double p0 = zone.getNearPoint();
            double p1 = zone.getZoneType().equals("DEMAND")
                    ? p0 + zone.getStrength() * p0 / 100.0
                    : p0 - zone.getStrength() * p0 / 100.0;
            double denom = Math.abs(p1 - p0);
            if (denom == 0) denom = 1e-9;

            double retracement = zone.getZoneType().equals("DEMAND") ?
                    (p1 - candleEntity.getClose()) / denom :
                    (candleEntity.getClose() - p1) / denom;

            if (Boolean.TRUE.equals(zone.getImpulseExtending())) {
                if (candles <= 3) {
                    // Extend the impulse if needed till 3 candles.
                    boolean impulseExtended = extendImpulse(zone, candleEntity, retracement);
                    if (impulseExtended) {
                        // Impulse not closed yet, shouldn't check for resilience and half life.
                        zoneRepository.save(zone);
                        return;
                    } else {
                        // Case where there is a retracement and it stopped the impulse.
                        // Should start for resilience and half life
                        candles = 1;
                    }
                } else {
                    zone.setImpulseExtending(false);
                    zoneRepository.save(zone);
                    return;
                }
            }

            if (candles <= 14) {

                // Update resilience
                if (retracement < 0) retracement = 0;
                if (zone.getResilience() == null || retracement > zone.getResilience()) {
                    zone.setResilience(retracement);
                }


                // Update half life
                Integer hl = zone.getHalfLife();
                if (hl == null || hl == -1) {
                    double halfTarget = zone.getZoneType().equals("DEMAND") ? p1 - denom / 2 : p1 + denom / 2;
                    boolean hit = zone.getZoneType().equals("DEMAND") ?
                            candleEntity.getClose() <= halfTarget :
                            candleEntity.getClose() >= halfTarget;
                    if (hit) {
                        zone.setHalfLife((int) candles);
                    } else {
                        zone.setHalfLife(-1);
                    }
                }
            }

            if (candles >= 14) {
                if (zone.getHalfLife() == -1) {
                    zone.setHalfLife(50);
                }
                if (zone.getResilience() == null) {
                    zone.setResilience(0.0);
                }
                updateAverages(zone);
            }

            zoneRepository.save(zone);
        }
    }

    public void updateAverages(Zone zone) {
        MarketIndicators indicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(
                zone.getStockSymbol(), zone.getTimeframe());
        if (indicators != null) {
            if (zone.getResilience() != null) {
                int resSamples = indicators.getResilienceSamples();
                double avgRes = indicators.getAverageResilience() == null ? 0.0 : indicators.getAverageResilience();
                indicators.setAverageResilience((avgRes * resSamples + zone.getResilience()) / (resSamples + 1));
                indicators.setResilienceSamples(resSamples + 1);
            }
            if (zone.getHalfLife() != null && zone.getHalfLife() > 0 && zone.getHalfLife() != 50) {
                int hlSamples = indicators.getHalfLifeSamples();
                double avgHL = indicators.getAverageHalfLife() == null ? 0.0 : indicators.getAverageHalfLife();
                indicators.setAverageHalfLife((avgHL * hlSamples + zone.getHalfLife()) / (hlSamples + 1));
                indicators.setHalfLifeSamples(hlSamples + 1);
            }
            marketIndicatorsRepository.save(indicators);
        }
    }

    private  boolean extendImpulse(Zone zone, CandleEntity candle, Double retracement) {
        double base = zone.getFarPoint();

        if (retracement >= 0.2) {
            zone.setImpulseExtending(false);
            zoneRepository.save(zone);
            return false;
        }
        if (zone.getZoneType().equals("DEMAND")) {
            double length = (candle.getHigh() - base) * 100.0 / base;
            if (length > zone.getStrength()) {
                zone.setStrength(length);
                double risk = (candle.getHigh() - base) / 3.5;
                zone.setRiskPerUnit(risk);
                zone.setNearPoint(base + risk);
            }

            zone.setIdentifiedAt(candle.getCandleTimestamp());
        } else {
            double length = (base - candle.getLow()) * 100.0 / base;
            if (length > zone.getStrength()) {
                zone.setStrength(length);
                double risk = (base - candle.getLow()) / 3.5;
                zone.setRiskPerUnit(risk);
                zone.setNearPoint(base - risk);
            }
            zone.setIdentifiedAt(candle.getCandleTimestamp());
        }

        return true;
    }
}
