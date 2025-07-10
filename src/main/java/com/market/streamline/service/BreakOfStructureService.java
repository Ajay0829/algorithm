package com.market.streamline.service;

import com.market.streamline.entity.structure.*;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import com.market.streamline.repository.SwingPointRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BreakOfStructureService {

    private final SwingPointRepository swingPointRepository;
    private final BreakOfStructureRepository breakOfStructureRepository;
    private final Environment env;
    private final ChartAnnotationService chartAnnotationService;
    private final MarketIndicatorsRepository marketIndicatorsRepository;

    public BreakOfStructureService(SwingPointRepository swingPointRepository, BreakOfStructureRepository breakOfStructureRepository, Environment env, ChartAnnotationService chartAnnotationService, MarketIndicatorsRepository marketIndicatorsRepository) {
        this.swingPointRepository = swingPointRepository;
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.env = env;
        this.chartAnnotationService = chartAnnotationService;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
    }

    public void checkForBreakOfStructure(CandleEntity candleEntity, boolean isHighCheck) {

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());
        if (marketIndicators == null) {
            return;
        }
        boolean breakOfStructure = false;
        double volatilityValue = marketIndicators.getVolatility();

        List<SwingPoint> swingPoints = swingPointRepository.findTop2ByStockSymbolAndTimeframeAndConfirmedTrueOrderByCandleTimestampDescIdDesc(candleEntity.getStockSymbol(), candleEntity.getTimeframe())
                .stream().sorted(Comparator.comparing(SwingPoint::getCandleTimestamp)
                        .thenComparing(SwingPoint::getId)).toList();
        if (swingPoints.size() < 2) {
            return;
        }

        SwingType lastSwingType = SwingType.valueOf(swingPoints.get(1).getSwingType());
        SwingPoint weakSwingPoint = swingPoints.get(0);

        if (lastSwingType == SwingType.HIGH && !isHighCheck) {
            breakOfStructure = priceMovedEnough(candleEntity, weakSwingPoint, volatilityValue, false);
        } else if (lastSwingType == SwingType.LOW && isHighCheck) {
            breakOfStructure = priceMovedEnough(candleEntity, weakSwingPoint, volatilityValue, true);
        }

        boolean bosExists = breakOfStructureRepository.existsByWeakSwingPointAndStrongSwingPoint(weakSwingPoint, swingPoints.get(1));
        if (bosExists) {
            return;
        }

        if (breakOfStructure) {
            SwingPoint strongSwingPoint = swingPoints.get(1);
            strongSwingPoint.setIsMajor(true);
            swingPointRepository.save(strongSwingPoint);
            chartAnnotationService.processSwingPoint(strongSwingPoint, "updated");
            BreakOfStructure bos = new BreakOfStructure(
                    candleEntity.getStockSymbol(),
                    candleEntity.getTimeframe(),
                    candleEntity.getCandleTimestamp(),
                    lastSwingType == SwingType.HIGH ? "BEARISH" : "BULLISH",
                    lastSwingType == SwingType.HIGH ? "LOW" : "HIGH",
                    weakSwingPoint,
                    strongSwingPoint,
                    candleEntity.getVolume()
            );
            breakOfStructureRepository.save(bos);
            chartAnnotationService.processBreakOfStructure(bos, "created");
        }
    }

    public double getMultiplier() {
        String key = "bos.threshold.multiplier";
        String value = env.getProperty(key);
        if (value == null) throw new IllegalArgumentException("No BOS threshold configured");
        return Double.parseDouble(value);
    }

    boolean priceMovedEnough(CandleEntity candleEntity, SwingPoint swingPoint, double volatilityValue, boolean direction) {
        double priceChange;
        if (direction) {
            priceChange = (candleEntity.getHigh() - swingPoint.getPrice()) * 100 / swingPoint.getPrice();
        } else {
            priceChange = (swingPoint.getPrice() - candleEntity.getLow()) * 100 / swingPoint.getPrice();
        }
        return priceChange >= getMultiplier() * volatilityValue;
    }
}