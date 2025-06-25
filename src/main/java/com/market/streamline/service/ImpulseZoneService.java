package com.market.streamline.service;

import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.Zone;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ImpulseZoneService {

    private final BreakOfStructureRepository breakOfStructureRepository;
    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;
    private final Environment env;

    public ImpulseZoneService(BreakOfStructureRepository breakOfStructureRepository, ZoneRepository zoneRepository, CandleRepository candleRepository, Environment env) {
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
        this.env = env;
    }

    // Instead of using BOS, use impulse to identify zones
    // Use BOS on the lower timeframe to identify zones on the higher timeframe
    public void detectHTFZone(SwingPoint swingPoint) {
        if (!swingPoint.getIsMajor()) {
            return;
        }

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

        String higherTimeframe;
        if (Objects.equals(bos.getTimeframe(), "1h")) {
            higherTimeframe = "1d";
        } else if (Objects.equals(bos.getTimeframe(), "15m")) {
            higherTimeframe = "1h";
        } else {
            return;
        }

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

        if (
                zoneRepository.existsByStrongSwingPointAndZoneType(
                        bosStart.getStrongSwingPoint(),
                        currentDirection.equals("BULLISH") ? "DEMAND" : "SUPPLY"
                )
        ) {
            return; // Zone already exists
        }



        Optional<Zone> zone = getZone(bosStart, bos, higherTimeframe, swingPoint);
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

    private Optional<Zone> getZone(BreakOfStructure start, BreakOfStructure end, String higherTimeframe, SwingPoint strongSwingPoint) {
        double zoneStrength = 0.0, zoneVolume = 0.0;

        // The current BOS LTF Candle
        CandleEntity ltfEndCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampOrderByCandleTimestampDesc(
                end.getStockSymbol(),
                end.getTimeframe(),
                end.getStrongSwingPoint().getCandleTimestamp()
        );
        double endPrice, startPrice;
        // For BULLISH, we take the high of the candle, for BEARISH we take the low
        startPrice = start.getStrongSwingPoint().getPrice();
        endPrice = strongSwingPoint.getPrice();

        // if price moved enough in the lower timeframe, then identify the zone
        if (!priceMovedEnough(startPrice, endPrice, end.getDirection().equals("BULLISH"), higherTimeframe)) {
            return Optional.empty();
        }

        // map the ltf candle to htf
        CandleEntity candleEntity = mapToHigherTimeframe(start.getStrongSwingPoint(), end, higherTimeframe);
        boolean isDemand = end.getDirection().equals("BULLISH");
        Zone zone = new Zone(
                end.getStockSymbol(),
                higherTimeframe,
                candleEntity.getCandleTimestamp(),
                isDemand ? "DEMAND" : "SUPPLY",
                isDemand ? candleEntity.getHigh() : candleEntity.getLow(),
                isDemand ? candleEntity.getLow() : candleEntity.getHigh(),
                "ACTIVE",
                zoneVolume,
                zoneStrength,
                start.getStrongSwingPoint() // Link the strong swing point
        );
        zoneRepository.save(zone);

        return Optional.of(zone);
    }

    // Helper to map a lower timeframe candle timestamp to the higher timeframe candle timestamp that contains it
    private CandleEntity mapToHigherTimeframe(SwingPoint ltfSwingPoint, BreakOfStructure bos, String higherTimeframe) {
        LocalDateTime ltfCandleTimestamp = ltfSwingPoint.getCandleTimestamp();
        CandleEntity ltfCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                bos.getTimeframe(),
                ltfCandleTimestamp
        );
        CandleEntity enclosingCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanEqualOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                higherTimeframe,
                ltfCandleTimestamp
        );

        if (enclosingCandle == null) {
            throw new IllegalArgumentException("No enclosing HTF candle found.");
        }

        CandleEntity previousCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                higherTimeframe,
                ltfCandleTimestamp
        );

        if (previousCandle == null) {
            throw new IllegalArgumentException("No previous HTF candle found before impulse.");
        }

        // choose between previousCandle and enclosingCandle based on the logic

        if (contains(ltfCandle, enclosingCandle) && contains(ltfCandle, previousCandle)) {
            if (bos.getDirection().equals("BULLISH")) {
                // Find the candle with lowest high for demand zone
                if (enclosingCandle.getHigh() <= previousCandle.getHigh()) {
                    return enclosingCandle;
                } else {
                    return previousCandle;
                }
            } else {
                if (enclosingCandle.getLow() >= previousCandle.getLow()) {
                    return enclosingCandle;
                } else {
                    return previousCandle;
                }
            }
        } else if (contains(ltfCandle, enclosingCandle)) {
            return enclosingCandle;
        } else if (contains(ltfCandle, previousCandle)) {
            return previousCandle;
        } else {
            // Ideally this should not happen, but if it does, return the enclosingCandle
            return enclosingCandle;
        }
    }

    boolean contains(CandleEntity ltfCandle, CandleEntity htfCandle) {
        return (ltfCandle.getLow() >= htfCandle.getLow() && ltfCandle.getHigh() <= htfCandle.getHigh());
    }

    boolean priceMovedEnough(double startPrice, double endPrice, boolean direction, String higherTimeframe) {
        double priceChange;

        if (direction) {
            priceChange = (endPrice - startPrice)*100 / startPrice;
        } else {
            priceChange = (startPrice - endPrice)*100 / startPrice;
        }

        return priceChange >= getImpulseThreshold(higherTimeframe);
    }

    public double getImpulseThreshold(String timeframe) {
        String key = "impulse.threshold." + timeframe;
        String value = env.getProperty(key);
        if (value == null) throw new IllegalArgumentException("No Impulse threshold configured for timeframe: " + timeframe);
        return Double.parseDouble(value);
    }

    // Fetch the 5 most recent BOS entries before or at the given timestamp
    public List<BreakOfStructure> getRecentBOS(String stockSymbol, String timeframe, LocalDateTime timestamp) {
        return breakOfStructureRepository
                .findTop5ByStockSymbolAndTimeframeAndCandleTimestampLessThanEqualOrderByCandleTimestampDesc(
                        stockSymbol, timeframe, timestamp
                );
    }
}
