package com.market.streamline.service;

import com.market.common.SwingType;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.SwingPointRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SwingPointService {

    private final int WINDOW_SIZE = 10;
    private final SwingPointRepository swingPointRepository;
    private final CandleRepository candleRepository;

    public SwingPointService(SwingPointRepository swingPointRepository, CandleRepository candleRepository) {
        this.swingPointRepository = swingPointRepository;
        this.candleRepository = candleRepository;
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

        // Fetch the latest swing point for this stock and timeframe
        Optional<SwingPoint> recentSwingPoint = swingPointRepository.findAll().stream()
                .filter(existing -> Objects.equals(existing.getStockSymbol(), sp.getStockSymbol()) &&
                        Objects.equals(existing.getTimeframe(), sp.getTimeframe()))
                .max(Comparator.comparing(SwingPoint::getCandleTimestamp));


        if (recentSwingPoint.isPresent()) {
            SwingPoint recent = recentSwingPoint.get();

            if (sp.getCandleTimestamp().isBefore(recent.getCandleTimestamp())) {
                return Optional.empty();
            }

            if (!Objects.equals(recent.getSwingType(), sp.getSwingType())) {
                // check if price move enough to consider it a new swing point
                if (Objects.equals(recent.getSwingType(), SwingType.HIGH.name())) {
                    if (priceMovedEnough(recent.getPrice(), sp.getPrice(), false)) {
                        swingPointRepository.save(sp);
                        // TODO: Send event for new swing point
                        return Optional.of(sp);
                    }
                } else {
                    if (priceMovedEnough(recent.getPrice(), sp.getPrice(), true)) {
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

        CandleEntity currentCandle = sortedCandles.get(WINDOW_SIZE);

        boolean isLeftCorrect = sortedCandles.subList(0, WINDOW_SIZE).stream()
                .allMatch(candle -> isInflectionPoint(candle, currentCandle, direction));
        boolean isRightCorrect = sortedCandles.subList(WINDOW_SIZE + 1, 2 * WINDOW_SIZE + 1).stream()
                .allMatch(candle -> isInflectionPoint(candle, currentCandle, direction));


            if (isLeftCorrect && isRightCorrect) {

                return Optional.of(
                        new SwingPoint(
                                currentCandle.getStockSymbol(),
                                currentCandle.getTimeframe(),
                                currentCandle.getCandleTimestamp(),
                                direction ? SwingType.HIGH.name() : SwingType.LOW.name(),
                                direction ? currentCandle.getHigh() : currentCandle.getLow()
                        )
                );
            }
        return Optional.empty();
    }

    private boolean isInflectionPoint(CandleEntity current, CandleEntity reference, boolean direction) {
        if (direction) {
            // check if reference is a high
            return current.getHigh() < reference.getHigh();
        } else {
            // check if reference is a low
            return current.getLow() > reference.getLow();
        }

    }

    private boolean priceMovedEnough(double price, double referencePrice, boolean direction) {
        double percentageMove = Math.abs(price - referencePrice) * 100 / price;
        double SWING_POINT_THRESHOLD = 10.0;
        return (direction == (price < referencePrice)) && percentageMove >= SWING_POINT_THRESHOLD;
    }
}
