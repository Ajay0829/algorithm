package com.market.streamline.service;

import com.market.streamline.entity.structure.*;
import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ImpulseZoneService {

    private final BreakOfStructureRepository breakOfStructureRepository;
    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;
    private final Environment env;
    private final ChartAnnotationService chartAnnotationService;
    private final TradeSimulationService tradeSimulationService;
    private final TradeRepository tradeRepository;
    private final MarketIndicatorsRepository marketIndicatorsRepository;

    public ImpulseZoneService(BreakOfStructureRepository breakOfStructureRepository, ZoneRepository zoneRepository, CandleRepository candleRepository, Environment env, ChartAnnotationService chartAnnotationService, TradeSimulationService tradeSimulationService, TradeRepository tradeRepository, MarketIndicatorsRepository marketIndicatorsRepository) {
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
        this.env = env;
        this.chartAnnotationService = chartAnnotationService;
        this.tradeSimulationService = tradeSimulationService;
        this.tradeRepository = tradeRepository;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
    }

    public void verifyZoneCorrectness(CandleEntity candleEntity) {
        Zone zone = zoneRepository.findTopByStockSymbolAndTimeframeAndIdentifiedAtOrderByIdDesc(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp()
        );
        if (zone == null) {
            return;
        }

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (marketIndicators == null) {
            return; // No volatility data available for the stock and timeframe
        }

        // Don't add overlapping zones.
        // Ideally we want to store the latest zone,
        // If an active trade exists for the old zone, then don't add the new zone.
        Optional<Zone> existingZoneOfSameType = zoneRepository.findLatestZoneByTypeActiveOrValid(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                zone.getZoneType(),
                candleEntity.getCandleTimestamp(),
                candleEntity.getCandleTimestamp().minusMonths(1)
        );

        double volatilityValue = marketIndicators.getVolatility();
        Double total_volume = candleRepository.maxVolumeBetweenTimestamps(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), zone.getCandleTimestamp(), zone.getIdentifiedAt());
        Long total_candles = candleRepository.countCandlesBetweenTimestamps(candleEntity.getStockSymbol(), candleEntity.getTimeframe(), zone.getCandleTimestamp(), zone.getIdentifiedAt());
        zone.setVolume(total_volume);
        zone.setStrength(Double.valueOf(total_candles));

        String zoneType = zone.getZoneType();
        boolean isSave = true;
        if (zoneType.equals("DEMAND")) {
            if (priceMovedEnough(zone.getFarPoint(), candleEntity.getClose(), volatilityValue, true)) {
                // Zone is valid, update the zone
                zone.setType("VALID");
                zoneRepository.save(zone);
            } else {
                // Zone is invalid, remove it
                zoneRepository.delete(zone);
                isSave = false;
            }
        } else {
            if (priceMovedEnough(zone.getFarPoint(), candleEntity.getClose(), volatilityValue, false)) {
                // Zone is valid, update the zone
                zone.setType("VALID");
                zoneRepository.save(zone);
            } else {
                zoneRepository.delete(zone);
                isSave = false;
            }
        }

        if (isSave) {

            if (existingZoneOfSameType.isPresent()) {
                Zone existingZone = existingZoneOfSameType.get();

                if (isOverlappingExistingZone(existingZone, zone.getNearPoint(), zone.getFarPoint())) {
                    Optional<Trade> existingTrade = tradeRepository.findByStockSymbolAndTimeframeAndZoneAndIsActiveTrue(
                            existingZone.getStockSymbol(),
                            existingZone.getTimeframe(),
                            existingZone
                    );

                    if (existingTrade.isPresent() && existingTrade.get().getIsActive()) {
                        zoneRepository.delete(zone);
                        return;
                    } else if (existingZone.getVolume() > zone.getVolume()){
                        zoneRepository.delete(zone);
                        return;
                    } else {
                        existingZone.setType("INVALID");
                        zoneRepository.save(existingZone);
                        chartAnnotationService.processZone(existingZone, "deleted");
                    }
                }
            }

            chartAnnotationService.processZone(zone, "created");
        }
    }


    public void invalidateZones(CandleEntity candleEntity, boolean isHighCheck) {
        Double currentPrice = isHighCheck ? candleEntity.getHigh() : candleEntity.getLow();
        LocalDateTime oneMonthAgoTimestamp = candleEntity.getCandleTimestamp().minusMonths(1);
        List<Zone> supplyZones = zoneRepository.findZonesByTypeWithFarPointPriceCondition(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                "SUPPLY",
                currentPrice,
                candleEntity.getCandleTimestamp(),
                oneMonthAgoTimestamp
        );

        List<Zone> demandZones = zoneRepository.findZonesByTypeWithFarPointPriceCondition(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                "DEMAND",
                currentPrice,
                candleEntity.getCandleTimestamp(),
                oneMonthAgoTimestamp
        );
        List<Zone> finalZones = Stream.concat(supplyZones.stream(), demandZones.stream()).toList();
        finalZones.forEach(zone -> {
            zone.setType("INVALID");
            zoneRepository.save(zone);
            chartAnnotationService.processZone(zone, "deleted");
        });
    }

    // Instead of using BOS, use impulse to identify zones
    // Use BOS on the lower timeframe to identify zones on the higher timeframe
    public void detectHTFZone(SwingPoint swingPoint) {
        if (!swingPoint.getIsMajor()) {
            return;
        }

        String higherTimeframe;
        if (Objects.equals(swingPoint.getTimeframe(), "1h")) {
            higherTimeframe = "1d";
        } else if (Objects.equals(swingPoint.getTimeframe(), "15m")) {
            higherTimeframe = "1h";
        } else {
            return;
        }

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(swingPoint.getStockSymbol(), higherTimeframe);
        if (marketIndicators == null) {
            return; // No volatility data available for the stock and timeframe
        }
        double volatilityValue = marketIndicators.getVolatility();

        Optional<BreakOfStructure> breakOfStructure = breakOfStructureRepository.findFirstByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
                swingPoint.getStockSymbol(),
                swingPoint.getTimeframe()
        );

        if (breakOfStructure.isEmpty()) {
            return; // No BOS found for the given stock and timeframe
        }
        BreakOfStructure bos = breakOfStructure.get();

        // Logic to identify a zone on HTF based on impulse in the lower timeframe.
        LocalDateTime ltfBOSTimestamp = bos.getCandleTimestamp();

        // Verify if I need to process this BOS
        boolean doesHigherTimeframeExist = candleRepository.existsByStockSymbolAndTimeframe(bos.getStockSymbol(), higherTimeframe);
        if (!doesHigherTimeframeExist) {
            return;
        }

        // Find Recent BOS list on the lower timeframe
        List<BreakOfStructure> lowerTimeFrameBosList = getRecentBOS(bos.getStockSymbol(), bos.getTimeframe(), ltfBOSTimestamp);

        if (lowerTimeFrameBosList.isEmpty()) {
            return;
        }

        BreakOfStructure bosStart = bos;
        String currentDirection = bos.getDirection();

        // check for strong trend in the lower timeframe
        for (BreakOfStructure lowerTimeFrameBos : lowerTimeFrameBosList) {
            if (Objects.equals(currentDirection, lowerTimeFrameBos.getDirection()) &&
                    lowerTimeFrameBos.getWeakSwingPoint().getIsMajor() &&
                    lowerTimeFrameBos.getStrongSwingPoint().getIsMajor()) {
                bosStart = lowerTimeFrameBos;
            } else if (Objects.equals(currentDirection, lowerTimeFrameBos.getDirection())){
                bosStart = lowerTimeFrameBos;
                break;
            } else {
                break;
            }
        }

//        if (zoneRepository.existsByStrongSwingPointAndZoneType(bosStart.getStrongSwingPoint(), currentDirection.equals("BULLISH") ? "DEMAND" : "SUPPLY")) {
//            // TODO: Update zone related parameters - Strength, Acceptance, Volume
//            return;
//        }

        Optional<Zone> zone = getZone(bosStart, bos, higherTimeframe, swingPoint, volatilityValue);
        if (zone.isEmpty()) {
            return;
        }
        if (
            zoneRepository.existsByStockSymbolAndTimeframeAndCandleTimestampAndZoneType(
                zone.get().getStockSymbol(),
                zone.get().getTimeframe(),
                zone.get().getCandleTimestamp(),
                zone.get().getZoneType()
            )
        ){
            return; // Zone already exists
        }

        zoneRepository.save(zone.get());
    }

    private Optional<Zone> getZone(BreakOfStructure start, BreakOfStructure end, String higherTimeframe, SwingPoint strongSwingPoint, double volatilityValue) {
        double zoneStrength = 0.0, zoneVolume = 0.0;
        double endPrice, startPrice;
        startPrice = start.getStrongSwingPoint().getPrice();
        endPrice = strongSwingPoint.getPrice();

        // Lower timeframe check
        if (!priceMovedEnough(startPrice, endPrice, volatilityValue, end.getDirection().equals("BULLISH"))) {
            return Optional.empty();
        }

        LocalDateTime zoneCandleTimestamp = mapTimestampToHigherTimeframe(start.getStrongSwingPoint());
        double actualPrice = start.getStrongSwingPoint().getPrice();
        double nearPoint, farPoint;
        boolean isDemand = end.getDirection().equals("BULLISH");
        if (isDemand) {
            nearPoint = actualPrice * (1 + 1*volatilityValue/100);
            farPoint = actualPrice;
        } else {
            nearPoint = actualPrice * ( 1 - 1*volatilityValue/100);
            farPoint = actualPrice;
        }
        LocalDateTime identifiedAt = mapTimestampToHigherTimeframe(strongSwingPoint);
        Zone zone = new Zone(
                end.getStockSymbol(),
                higherTimeframe,
                zoneCandleTimestamp, // actual candle where impulse started
                isDemand ? "DEMAND" : "SUPPLY",
                nearPoint,
                farPoint,
                "NA",
                zoneVolume,
                zoneStrength,
                0, // No of taps starts at 0
                identifiedAt // When the zone was identified
        );
        return Optional.of(zone);
    }

    private boolean isOverlappingExistingZone(Zone existingZone, double nearPoint, double farPoint) {
        // Normalize the zone boundaries to ensure min <= max for both zones
        double existingMin = Math.min(existingZone.getNearPoint(), existingZone.getFarPoint());
        double existingMax = Math.max(existingZone.getNearPoint(), existingZone.getFarPoint());
        double newMin = Math.min(nearPoint, farPoint);
        double newMax = Math.max(nearPoint, farPoint);

        // Check for overlap: two ranges overlap if one range's min is <= other range's max
        // and the other range's min is <= the first range's max
        return !(newMax < existingMin || newMin > existingMax);
    }

    boolean priceMovedEnough(double startPrice, double endPrice, double volatilityValue, boolean direction) {
        double priceChange;

        if (direction) {
            priceChange = (endPrice - startPrice)*100 / startPrice;
        } else {
            priceChange = (startPrice - endPrice)*100 / startPrice;
        }

        return priceChange >= getMultiplier() * volatilityValue;
    }

    public double getMultiplier() {
        String key = "impulse.threshold.multiplier";
        String value = env.getProperty(key);
        if (value == null) throw new IllegalArgumentException("No Impulse threshold configured");
        return Double.parseDouble(value);
    }

    // Fetch the 5 most recent BOS entries before or at the given timestamp
    public List<BreakOfStructure> getRecentBOS(String stockSymbol, String timeframe, LocalDateTime timestamp) {
        return breakOfStructureRepository
                .findTop5ByStockSymbolAndTimeframeAndCandleTimestampLessThanEqualOrderByCandleTimestampDesc(
                        stockSymbol, timeframe, timestamp
                );
    }

    public LocalDateTime mapTimestampToHigherTimeframe(SwingPoint strongSwingPoint) {
        LocalDateTime ltfTimestamp = strongSwingPoint.getCandleTimestamp();
        String higherTimeframe;
        if (Objects.equals(strongSwingPoint.getTimeframe(), "15m")) {
            higherTimeframe = "1h";
        } else if (Objects.equals(strongSwingPoint.getTimeframe(), "1h")) {
            higherTimeframe = "1d";
        } else {
            throw new IllegalArgumentException("Unsupported timeframe: " + strongSwingPoint.getTimeframe());
        }

        if (higherTimeframe.endsWith("h")) {
            int hour = ltfTimestamp.getHour();
            int minute = ltfTimestamp.getMinute();

            if (hour >= 9 && hour <= 15) {
                int htfHour;
                if (minute == 0) {
                    htfHour = hour - 1;
                } else {
                    htfHour = hour;
                }
                return ltfTimestamp
                    .withHour(htfHour)
                    .withMinute(15)
                    .withSecond(0)
                    .withNano(0);
            } else {
                return ltfTimestamp
                    .withHour(9)
                    .withMinute(15)
                    .withSecond(0)
                    .withNano(0);
            }
        } else {
            return ltfTimestamp
                .withHour(9)
                .withMinute(15)
                .withSecond(0)
                .withNano(0);
        }
    }
}
