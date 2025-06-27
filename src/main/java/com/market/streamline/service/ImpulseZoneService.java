package com.market.streamline.service;

import com.market.streamline.dto.ChartZoneDTO;
import com.market.streamline.entity.*;
import com.market.streamline.kafka.ChartAnnotationProducer;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.VolatilityRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ImpulseZoneService {

    private final BreakOfStructureRepository breakOfStructureRepository;
    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;
    private final Environment env;
    private final VolatilityRepository volatilityRepository;
    private final ChartAnnotationProducer chartAnnotationProducer;

    public ImpulseZoneService(BreakOfStructureRepository breakOfStructureRepository, ZoneRepository zoneRepository, CandleRepository candleRepository, Environment env, VolatilityRepository volatilityRepository, ChartAnnotationProducer chartAnnotationProducer) {
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
        this.env = env;
        this.volatilityRepository = volatilityRepository;
        this.chartAnnotationProducer = chartAnnotationProducer;
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

        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe()
        );

        if (volatility == null) {
            return; // No volatility data available for the stock and timeframe
        }

        double volatilityValue = volatility.getVolatility();


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

        LocalDateTime nextCandleTimestamp;
        if (zone.getTimeframe().equals("1d")) {
            nextCandleTimestamp = zone.getCandleTimestamp().plusDays(15);
        } else {
            nextCandleTimestamp = zone.getCandleTimestamp().plusHours(15);
        }

        chartAnnotationProducer.sendAnnotation(
                new ChartZoneDTO(
                        "zone",
                        isSave ? "created" : "deleted",
                        new ChartZoneDTO.ZoneData(
                                zone.getZoneType(),
                                zone.getNearPoint(),
                                zone.getFarPoint(),
                                zone.getCandleTimestamp().toString(),
                                nextCandleTimestamp.toString(),
                                zone.getTimeframe()
                        )
                )
        );
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


        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(
                swingPoint.getStockSymbol(),
                higherTimeframe
        );
        if (volatility == null) {
            return; // No volatility data available for the stock and timeframe
        }

        double volatilityValue = volatility.getVolatility();

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

        if (zoneRepository.existsByStrongSwingPointAndZoneType(bosStart.getStrongSwingPoint(), currentDirection.equals("BULLISH") ? "DEMAND" : "SUPPLY")) {
            return;
        }

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
            nearPoint = actualPrice * (1 + 1.5*volatilityValue/100);
            farPoint = actualPrice;
        } else {
            nearPoint = actualPrice * ( 1 - 1.5*volatilityValue/100);
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
                "ACTIVE",
                zoneVolume,
                zoneStrength,
                start.getStrongSwingPoint(), // Link the strong swing point
                0, // No of taps starts at 0
                identifiedAt // When the zone was identified
        );
        zoneRepository.save(zone);
        return Optional.of(zone);
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
        ZoneId usMarketZone = ZoneId.of("America/New_York");
        if (higherTimeframe.endsWith("h")) {
            // Map to start of the hour
            return ltfTimestamp.atZone(usMarketZone)
                .withMinute(0).withSecond(0).withNano(0)
                .toLocalDateTime();
        } else {
            // Map to start of the day
            return ltfTimestamp.atZone(usMarketZone)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .toLocalDateTime();
        }
    }
}
