package com.market.streamline.service;

import com.market.common.SwingType;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.SwingPointRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SwingPointService {

    private final int WINDOW_SIZE = 10;
    private final SwingPointRepository swingPointRepository;
    private final CandleRepository candleRepository;
    private final BreakOfStructureRepository breakOfStructureRepository;
    private final Environment env;

    public SwingPointService(SwingPointRepository swingPointRepository, CandleRepository candleRepository, BreakOfStructureRepository breakOfStructureRepository, Environment env) {
        this.swingPointRepository = swingPointRepository;
        this.candleRepository = candleRepository;
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.env = env;
    }

    public Optional<SwingPoint> checkForSwingPoint(CandleEntity candleEntity) {

        // Fetch 2*N CandleEntity candles before this timestamp for the timeframe and stock
        List<CandleEntity> candleEntities = candleRepository.findRecentCandles(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp(),
                PageRequest.of(0, 2 * WINDOW_SIZE + 1)
        );
        if (candleEntities.size() < 2 * WINDOW_SIZE + 1) {
            return Optional.empty();
        }

        // Sort by candleTimestamp ascending
        List<CandleEntity> sortedCandles = candleEntities.stream()
                .sorted(Comparator.comparing(CandleEntity::getCandleTimestamp))
                .collect(Collectors.toList());

        Optional<SwingPoint> swingPoint = detectSwingPoint(sortedCandles);
        return swingPoint.flatMap(this::saveAndGet);
    }

    private Optional<SwingPoint> saveAndGet(SwingPoint sp) {
        // Use repository method to check if an identical swing point already exists
        Optional<SwingPoint> existingSwingPoint = swingPointRepository
                .findByStockSymbolAndTimeframeAndSwingTypeAndCandleTimestamp(
                        sp.getStockSymbol(),
                        sp.getTimeframe(),
                        sp.getSwingType(),
                        sp.getCandleTimestamp()
                );
        if (existingSwingPoint.isPresent()) {
            return existingSwingPoint;
        }

        Optional<SwingPoint> recentSwingPoint = swingPointRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
                sp.getStockSymbol(), sp.getTimeframe()
        );

        // Find the latest strong swing point (confirmed swing point of opposite type)
        Optional<SwingPoint> latestStrongSwingPoint = swingPointRepository.findTopByStockSymbolAndTimeframeAndSwingTypeAndConfirmedTrueOrderByCandleTimestampDesc(
            sp.getStockSymbol(),
            sp.getTimeframe(),
            SwingType.HIGH.name().equals(sp.getSwingType()) ? SwingType.LOW.name() : SwingType.HIGH.name()
        );

        // Find the latest BOS for this symbol and timeframe
        Optional<BreakOfStructure> latestBOS = breakOfStructureRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
            sp.getStockSymbol(),
            sp.getTimeframe()
        );
        boolean isMajor = false;

        if (latestStrongSwingPoint.isPresent() && latestBOS.isPresent()) {
            if (latestBOS.get().getCandleTimestamp().isAfter(latestStrongSwingPoint.get().getCandleTimestamp())) {
                isMajor = true;
            }
        }
        sp.setIsMajor(isMajor);

        if (recentSwingPoint.isPresent()) {
            SwingPoint recent = recentSwingPoint.get();

            if (sp.getCandleTimestamp().isBefore(recent.getCandleTimestamp())) {
                return Optional.empty();
            }

            if (!Objects.equals(recent.getSwingType(), sp.getSwingType())) {
                // check if price move enough to consider it a new swing point
                if (Objects.equals(recent.getSwingType(), SwingType.HIGH.name())) {
                    if (priceMovedEnough(recent.getPrice(), sp.getPrice(), sp.getTimeframe(), false)) {
                        swingPointRepository.save(sp);
                        // TODO: Send event for new swing point
                        return Optional.of(sp);
                    }
                } else {
                    if (priceMovedEnough(recent.getPrice(), sp.getPrice(), sp.getTimeframe(), true)) {
                        // TODO: Send event for new swing point
                        swingPointRepository.save(sp);
                        return Optional.of(sp);
                    }
                }
                return Optional.empty();
            } else if (shouldReuseExistingSwingPoint(recent, sp)) { // Same swing type, check if we can reuse
                return Optional.of(recent);
            } else { // If not reuse, delete the recent one and save the new swing point
                swingPointRepository.delete(recent);
                // TODO: Send event for new swing point
                swingPointRepository.save(sp);
                return Optional.of(sp);
            }
        } else {
            // TODO: Send event for new swing point
            swingPointRepository.save(sp);
            return Optional.of(sp);
        }
    }

    private boolean shouldReuseExistingSwingPoint(SwingPoint existing, SwingPoint newEntry) {
        if (SwingType.HIGH.name().equals(existing.getSwingType())) {
            return newEntry.getPrice() < existing.getPrice();
        } else {
            return newEntry.getPrice() > existing.getPrice();
        }
    }

    private Optional<SwingPoint> detectSwingPoint(List<CandleEntity> sortedCandles) {
        Optional<SwingPoint> detectedHigh = getSwingPoint(sortedCandles, true);
        if (detectedHigh.isPresent()) {
            return detectedHigh;
        }

        return getSwingPoint(sortedCandles, false);
    }

    private Optional<SwingPoint> getSwingPoint(List<CandleEntity> sortedCandles, boolean direction) {
        CandleEntity currentCandle = sortedCandles.get(sortedCandles.size() - 1);
        boolean isLeftCorrect = sortedCandles.stream()
                .allMatch(candle -> isInflectionPoint(candle, currentCandle, direction));

        if (isLeftCorrect) {
            return Optional.of(
                    new SwingPoint(
                            currentCandle.getStockSymbol(),
                            currentCandle.getTimeframe(),
                            currentCandle.getCandleTimestamp(),
                            direction ? SwingType.HIGH.name() : SwingType.LOW.name(),
                            direction ? currentCandle.getHigh() : currentCandle.getLow(),
                            false,
                            false
                    )
            );
        }
        return Optional.empty();
    }

    private boolean isInflectionPoint(CandleEntity current, CandleEntity reference, boolean direction) {
        if (direction) {
            // check if reference is a high
            return current.getHigh() <= reference.getHigh();
        } else {
            // check if reference is a low
            return current.getLow() >= reference.getLow();
        }

    }

    private boolean priceMovedEnough(double price, double referencePrice, String timeframe, boolean direction) {
        double percentageMove = Math.abs(price - referencePrice) * 100 / price;
        double SWING_POINT_THRESHOLD = getSwingThreshold(timeframe);
        return (direction == (price < referencePrice)) && percentageMove >= SWING_POINT_THRESHOLD;
    }

    public double getSwingThreshold(String timeframe) {
        String key = "swing.threshold." + timeframe;
        String value = env.getProperty(key);
        if (value == null) throw new IllegalArgumentException("No swing threshold configured for timeframe: " + timeframe);
        return Double.parseDouble(value);
    }
}
