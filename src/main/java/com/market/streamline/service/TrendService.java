package com.market.streamline.service;

import com.market.streamline.entity.structure.SwingType;
import com.market.streamline.entity.structure.BreakOfStructure;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.SwingPoint;
import com.market.streamline.entity.structure.Trend;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.SwingPointRepository;
import com.market.streamline.repository.TrendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TrendService {

    @Autowired
    private SwingPointRepository swingPointRepository;
    @Autowired
    private TrendRepository trendRepository;
    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;
    @Autowired
    private CandleRepository candleRepository;

    public void updateTrendStrength(CandleEntity candleEntity) {
        Trend trend = trendRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe()
        );
        if (trend != null) {
            SwingPoint strongSwingPoint = trend.getStrongSwingPoint();
            double strength = calculateStrength(strongSwingPoint, candleEntity);
            if (trend.getStrength() <= strength) {
                trend.setStrength(strength);
                trendRepository.save(trend);
            }
        }
    }

    public void updateTrend(BreakOfStructure breakOfStructure) {
        Trend trend = trendRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
                breakOfStructure.getStockSymbol(),
                breakOfStructure.getTimeframe()
        );
        if (trend == null) {
            saveTrendToDb(
                    breakOfStructure.getStrongSwingPoint(),
                    breakOfStructure,
                    Objects.equals(breakOfStructure.getDirection(), "BULLISH") ? "UP" : "DOWN"
            );
        } else {
            if (breakOfStructure.getDirection().equals("BULLISH")) {
                if (!trend.getType().equals("UP")) {
                    saveTrendToDb(breakOfStructure.getStrongSwingPoint(), breakOfStructure, "UP");
                }
            } else {
                if (!trend.getType().equals("DOWN")) {
                    saveTrendToDb(breakOfStructure.getStrongSwingPoint(), breakOfStructure, "DOWN");
                }
            }
        }

    }

    private void saveTrendToDb(SwingPoint swingPoint, BreakOfStructure breakOfStructure, String trendType) {
        CandleEntity candleEntity = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampOrderByCandleTimestampDesc(
                swingPoint.getStockSymbol(),
                swingPoint.getTimeframe(),
                breakOfStructure.getCandleTimestamp()
        );

        Trend trend = new Trend(
                swingPoint.getStockSymbol(),
                swingPoint.getTimeframe(),
                breakOfStructure.getCandleTimestamp(),
                trendType,
                calculateStrength(swingPoint, candleEntity),
                swingPoint
        );
        trendRepository.save(trend);
    }

    private double calculateStrength(SwingPoint swingPoint, CandleEntity candleEntity) {
        if (swingPoint.getSwingType().equals(SwingType.LOW.name())) {
            return (candleEntity.getHigh() - swingPoint.getPrice()) * 100 / swingPoint.getPrice();
        } else {
            return (swingPoint.getPrice() - candleEntity.getLow()) * 100 / swingPoint.getPrice();
        }
    }
}
