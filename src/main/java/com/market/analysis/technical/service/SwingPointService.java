package com.market.analysis.technical.service;

import com.market.analysis.technical.model.PriceRange;
import com.market.common.SwingPoint;
import com.market.common.SwingType;
import com.market.external.polygon.dto.Candle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SwingPointService {

    private static final double SWING_POINT_THRESHOLD = 5;
    private static final int SWING_WINDOW_SIZE = 15;

    public List<SwingPoint> getSwingPoints(List<Candle> candles) {
        List<SwingPoint> detectedSwingPoints = detectSwingPoints(candles);
        return getFilteredSwingPoints(detectedSwingPoints);
    }

    private List<SwingPoint> getFilteredSwingPoints(List<SwingPoint> swingPoints) {
        if (swingPoints.size() < 2) {
            return swingPoints;
        }

        List<SwingPoint> filteredSwingPoints = new ArrayList<>();

        int swingPointIndex = 0;

        while (swingPointIndex < swingPoints.size()) {
            List<SwingPoint> currentCandidates = new ArrayList<>();
            SwingPoint currentSwingPoint = swingPoints.get(swingPointIndex);
            SwingType currentSwingType = currentSwingPoint.getSwingType();

            currentCandidates.add(currentSwingPoint);

            swingPointIndex++;

            while (swingPointIndex < swingPoints.size() && swingPoints.get(swingPointIndex).getSwingType() == currentSwingType) {
                currentCandidates.add(swingPoints.get(swingPointIndex++));
            }

            filteredSwingPoints.add(evaluateSwingPoint(currentCandidates));
        }

        return filteredSwingPoints;
    }

    private SwingPoint evaluateSwingPoint(List<SwingPoint> swingPoints) {
        if (swingPoints.size() < 2) { return swingPoints.get(0); }

        SwingType type = swingPoints.get(0).getSwingType();
        boolean allSameType = swingPoints.stream().allMatch(sp -> sp.getSwingType() == type);
        if (!allSameType) {
            throw new IllegalArgumentException("All swing points must be of the same SwingType");
        }

        if (type == SwingType.LOW) {
            return swingPoints.stream().min(Comparator.comparingDouble(SwingPoint::getPrice)).get();
        } else {
            return swingPoints.stream().max(Comparator.comparingDouble(SwingPoint::getPrice)).get();
        }
    }

    private List<SwingPoint> detectSwingPoints(List<Candle> candles) {

        List<SwingPoint> results = new ArrayList<>();

        List<PriceRange> prices = candles.stream()
                .map(candle -> new PriceRange(candle.getHigh(), candle.getLow()))
                .collect(Collectors.toList());

        for (int i = 0; i < prices.size(); i++) {
            int leftIndex = Math.max(0, i - SWING_WINDOW_SIZE);
            int rightIndex = Math.min(prices.size() - 1, i + SWING_WINDOW_SIZE);
            int highIndex = leftIndex;
            int lowIndex = leftIndex;

            for (int j = leftIndex; j <= rightIndex; j++) {
                if (prices.get(j).getHigh() > prices.get(highIndex).getHigh()) {
                    highIndex = j;
                }
                if (prices.get(j).getLow() < prices.get(lowIndex).getLow()) {
                    lowIndex = j;
                }
            }

            List<PriceRange> leftPriceRange = prices.subList(leftIndex, i);
            List<PriceRange> rightPriceRange = prices.subList(i, rightIndex + 1);

            if (highIndex == i) {
                // Check if the current candle is a swing high
                double leftLow = leftPriceRange.isEmpty() ? prices.get(i).getLow()
                        : Collections.min(leftPriceRange, Comparator.comparingDouble(PriceRange::getLow)).getLow();
                double rightLow = rightPriceRange.isEmpty() ? prices.get(i).getLow()
                        : Collections.min(rightPriceRange, Comparator.comparingDouble(PriceRange::getLow)).getLow();

                if (priceMovedEnough(leftLow, prices.get(i).getHigh()) || priceMovedEnough(prices.get(i).getHigh(), rightLow)) {
                    results.add(new SwingPoint(i, prices.get(i).getHigh(), SwingType.HIGH));
                }

            } else if (lowIndex == i) {
                // Check if the current candle is a swing low
                double leftHigh = leftPriceRange.isEmpty() ? prices.get(i).getHigh()
                        : Collections.max(leftPriceRange, Comparator.comparingDouble(PriceRange::getHigh)).getHigh();
                double rightHigh = rightPriceRange.isEmpty() ? prices.get(i).getHigh()
                        : Collections.max(rightPriceRange, Comparator.comparingDouble(PriceRange::getHigh)).getHigh();

                if (priceMovedEnough(leftHigh, prices.get(i).getLow()) || priceMovedEnough(prices.get(i).getLow(), rightHigh)) {
                    results.add(new SwingPoint(i, prices.get(i).getLow(), SwingType.LOW));
                }
            }
        }

        return results;
    }

    private boolean priceMovedEnough(double price, double referencePrice) {
        double percentageMove = Math.abs(price - referencePrice)*100 / referencePrice;
        return percentageMove >= SWING_POINT_THRESHOLD;
    }
}
