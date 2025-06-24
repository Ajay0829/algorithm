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

import java.time.LocalDateTime;
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

    public void confirmSwingPointIfAny(CandleEntity candleEntity, boolean isHighCheck) {
        Optional<SwingPoint> recentSwingPoint = swingPointRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDescIdDesc(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe()
        );

        if (recentSwingPoint.isPresent()) {

            SwingPoint recent = recentSwingPoint.get();
            if (!recent.getConfirmed()) {
                if (isHighCheck) {
                    if (recent.getSwingType().equals(SwingType.LOW.name())) {
                        if (priceMovedEnough(recent.getPrice(), candleEntity.getHigh(), candleEntity.getTimeframe(), true)) {
                            recent.setConfirmed(true);
                            swingPointRepository.save(recent);
                        }
                    }
                } else {
                    if (recent.getSwingType().equals(SwingType.HIGH.name())) {
                        if (priceMovedEnough(recent.getPrice(), candleEntity.getLow(), candleEntity.getTimeframe(), false)) {
                            recent.setConfirmed(true);
                            swingPointRepository.save(recent);
                        }
                    }
                }
            }
        }
    }

    public Optional<SwingPoint> checkForSwingPoint(CandleEntity candleEntity, boolean isHighCheck) {

        // Fetch 2*N CandleEntity candles before this timestamp for the timeframe and stock
        List<CandleEntity> candleEntities = candleRepository.findRecentCandles(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                candleEntity.getCandleTimestamp(),
                PageRequest.of(0, WINDOW_SIZE + 1)
        );
        if (candleEntities.size() <  WINDOW_SIZE + 1) {
            return Optional.empty();
        }

        // Sort by candleTimestamp ascending
        List<CandleEntity> sortedCandles = candleEntities.stream()
                .sorted(Comparator.comparing(CandleEntity::getCandleTimestamp))
                .collect(Collectors.toList());

        Optional<SwingPoint> swingPoint = detectSwingPoint(sortedCandles, isHighCheck);
        if (swingPoint.isPresent()) {
            return saveAndGet(swingPoint.get(), isHighCheck);
        }
        return Optional.empty();
    }

    protected Optional<SwingPoint> saveAndGet(SwingPoint sp, boolean isHighCheck) {
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

        Optional<SwingPoint> recentSwingPoint = swingPointRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDescIdDesc(
                sp.getStockSymbol(), sp.getTimeframe()
        );

        boolean isMajorSwingPoint = checkForMajorSwingPoint(sp);
        sp.setIsMajor(isMajorSwingPoint);

        if (recentSwingPoint.isPresent()) {
            SwingPoint recent = recentSwingPoint.get();

            if (sp.getCandleTimestamp().isBefore(recent.getCandleTimestamp())) {
                return Optional.empty();
            }

            return handleRecentSwingPoint(sp, recent, isHighCheck);
        } else {
            // TODO: Send event for new swing point
            swingPointRepository.save(sp);
            return Optional.of(sp);
        }
    }

    private Optional<SwingPoint> handleRecentSwingPoint(SwingPoint sp, SwingPoint recent, boolean isHighCheck) {

        if (isHighCheck) {
            if (Objects.equals(recent.getSwingType(), SwingType.HIGH.name())) {
                // recent is swing high
                if (recent.getPrice() >= sp.getPrice()) {
                    return Optional.of(recent);
                } else {
                    swingPointRepository.delete(recent);
                    swingPointRepository.save(sp);
                    return Optional.of(sp);
                }
            } else {
                // recent is swing low
                if (priceMovedEnough(recent.getPrice(), sp.getPrice(), sp.getTimeframe(), true)) {
                    swingPointRepository.save(sp);
                    return Optional.of(sp);
                }
            }
        } else {
            if (Objects.equals(recent.getSwingType(), SwingType.LOW.name())) {
                // recent is swing low
                if (recent.getPrice() <= sp.getPrice()) {
                    return Optional.of(recent);
                } else {
                    swingPointRepository.delete(recent);
                    swingPointRepository.save(sp);
                    return Optional.of(sp);
                }
            } else {
                // recent is swing high
                if (priceMovedEnough(recent.getPrice(), sp.getPrice(), sp.getTimeframe(), false)) {
                    swingPointRepository.save(sp);
                    return Optional.of(sp);
                }
            }
        }

        return Optional.empty();
    }

    private boolean checkForMajorSwingPoint(SwingPoint sp) {
        // Find the latest strong swing point (confirmed swing point of opposite type)
        Optional<SwingPoint> latestStrongSwingPoint = swingPointRepository.findTopByStockSymbolAndTimeframeAndSwingTypeOrderByCandleTimestampDescIdDesc(
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

        if (latestBOS.isPresent() && latestStrongSwingPoint.isPresent()) {
            if (latestBOS.get().getDirection().equals("BULLISH") && sp.getSwingType().equals(SwingType.HIGH.name())) {
                LocalDateTime strongSwingPointTimeStamp = latestStrongSwingPoint.get().getCandleTimestamp();
                LocalDateTime bosSwingPointTimeStamp = latestBOS.get().getCandleTimestamp();
                if (bosSwingPointTimeStamp.isAfter(strongSwingPointTimeStamp) || bosSwingPointTimeStamp.isEqual(strongSwingPointTimeStamp)) {
                    isMajor = true;
                }
            } else if (latestBOS.get().getDirection().equals("BEARISH") && sp.getSwingType().equals(SwingType.LOW.name())) {
                LocalDateTime strongSwingPointTimeStamp = latestStrongSwingPoint.get().getCandleTimestamp();
                LocalDateTime bosSwingPointTimeStamp = latestBOS.get().getCandleTimestamp();
                if (bosSwingPointTimeStamp.isAfter(strongSwingPointTimeStamp) || bosSwingPointTimeStamp.isEqual(strongSwingPointTimeStamp)) {
                    isMajor = true;
                }
            }
        }

        return isMajor;
    }

    private Optional<SwingPoint> detectSwingPoint(List<CandleEntity> sortedCandles, boolean isHighCheck) {
        if (isHighCheck) {
            // Check if the last index high is the highest of all
            return getSwingPoint(sortedCandles, true);
        } else {
            // Check if the last index low is the lowest of all
            return getSwingPoint(sortedCandles, false);
        }
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
