package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.Double.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;

@Service
public class ImbalanceDetectionService {

    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;
    private final MarketIndicatorsRepository marketIndicatorsRepository;
    private final Environment env;

    public ImbalanceDetectionService(ZoneRepository zoneRepository, CandleRepository candleRepository, MarketIndicatorsRepository marketIndicatorsRepository, Environment env) {
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
        this.env = env;
    }

    public void detectImbalance(CandleEntity candleEntity) {
        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());

        if (marketIndicators == null) {
            return;
        }

        double average_volatility = marketIndicators.getAverageVolatility();
        double impulse_multiplier = getMultiplier();

        if (marketIndicators.getNoOfSamples() < 10) return;

        double percentage_target = average_volatility*impulse_multiplier;

        Optional<Zone> mostRecentZone = zoneRepository.findLatestZone(candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        long totalCandlesToFetch;
        if (mostRecentZone.isEmpty()) {
            totalCandlesToFetch = 6;
        } else {
            totalCandlesToFetch = candleRepository.countCandlesBetweenTimestamps(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), mostRecentZone.get().getIdentifiedAt(), candleEntity.getCandleTimestamp());
            totalCandlesToFetch = min(totalCandlesToFetch, 6);
        }

        List<CandleEntity> recentCandlesBeforeSort = candleRepository.getRecentCandlesByNumber(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), totalCandlesToFetch);

        List<CandleEntity> recentCandles = recentCandlesBeforeSort.stream().sorted(Comparator.comparing(CandleEntity::getCandleTimestamp)).toList();

        double total_move = 0;
        int index = -1;
        for (int j = recentCandles.size() - 1; j > 0; j--) {
            double current_price_move = (recentCandles.get(j).getClose() - recentCandles.get(j-1).getClose())*100/recentCandles.get(j-1).getClose();
            total_move += current_price_move;

            if (abs(total_move) >= percentage_target) {
                index = j;
                break;
            }
        }

        if (index == -1) return;

        // Found an impulse, should we add it or not??

        Optional<Zone> existingZoneOpt = zoneRepository.findLatestZoneByType(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                total_move > 0 ? "DEMAND" : "SUPPLY",
                candleEntity.getCandleTimestamp(),
                candleEntity.getCandleTimestamp().minusMonths(1)
        );

        Zone newZone = getNewZone(candleEntity, recentCandles, index, total_move > 0 ? "DEMAND" : "SUPPLY", average_volatility);

        if (existingZoneOpt.isEmpty()) {
            zoneRepository.save(newZone);
        } else {
            Zone existingZone = existingZoneOpt.get();
            Double existingZoneFarPoint = existingZone.getFarPoint();
            Double currentZoneFarPoint = newZone.getFarPoint();

            double percentage_move = (existingZoneFarPoint - currentZoneFarPoint)*100/existingZoneFarPoint;
            if (abs(percentage_move) >= getMultiplier()*average_volatility) {
                // If percentage move >= 3*V then add new zone
                zoneRepository.save(newZone);
            } else {
                // If percentage move < 3*V store zone with highest strength
                // TODO: Verify the assumption of ExistingZone trades are already closed before new zone formation if any
                double existingZoneStrength = existingZone.getStrength()*existingZone.getVolume()/(average_volatility*marketIndicators.getAverageVolume());
                double newZoneStrength = newZone.getStrength()*newZone.getVolume()/(average_volatility*marketIndicators.getAverageVolume());

                if (abs(newZoneStrength) > abs(existingZoneStrength)) {
                    existingZone.setType("INVALID");
                    zoneRepository.save(existingZone);
                    zoneRepository.save(newZone);
                }
            }
        }
    }

    public Zone getNewZone(CandleEntity candleEntity, List<CandleEntity> recentCandles, int index, String zoneType, double volatility) {
        CandleEntity impulseStart = recentCandles.get(index - 1);
        CandleEntity impulseEnd = recentCandles.get(recentCandles.size() - 1);
        double nearPoint, farPoint;
        if (zoneType.equals("DEMAND")) {
            farPoint = impulseStart.getClose();
            nearPoint = impulseStart.getClose() + impulseStart.getLow()*volatility/100;
        } else {
            farPoint = impulseStart.getClose();
            nearPoint = impulseStart.getClose() - impulseStart.getClose() * volatility / 100;
        }
        double max_volume = 0.0;
        for (int i = index; i < recentCandles.size(); i++) {
            max_volume = max(max_volume, recentCandles.get(i).getVolume());
        }

        double total_price_move = (impulseEnd.getClose() - recentCandles.get(index - 1).getClose())*100/recentCandles.get(index - 1).getClose();

        return new Zone(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                recentCandles.get(index).getCandleTimestamp(),
                zoneType,
                nearPoint,
                farPoint,
                "VALID",
                max_volume,
                total_price_move,
                0, // No of taps starts at 0
                candleEntity.getCandleTimestamp()
        );
    }

    public double getMultiplier() {
        String key = "impulse.threshold.multiplier";
        String value = env.getProperty(key);
        if (value == null) throw new IllegalArgumentException("No Impulse threshold configured");
        return Double.parseDouble(value);
    }
}
