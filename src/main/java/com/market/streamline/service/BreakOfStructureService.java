package com.market.streamline.service;

import com.market.common.SwingType;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.kafka.BOSEventProducer;
import com.market.streamline.model.BOSEvent;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.SwingPointRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BreakOfStructureService {

    private final BOSEventProducer bosEventProducer;
    private final SwingPointRepository swingPointRepository;
    private final BreakOfStructureRepository breakOfStructureRepository;
    private final Environment env;

    public BreakOfStructureService(BOSEventProducer bosEventProducer, SwingPointRepository swingPointRepository, BreakOfStructureRepository breakOfStructureRepository, Environment env) {
        this.bosEventProducer = bosEventProducer;
        this.swingPointRepository = swingPointRepository;
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.env = env;
    }

    public void checkForBreakOfStructure(CandleEntity candleEntity) {

        boolean breakOfStructure = false;

        Optional<SwingPoint> recentSwingPoint = swingPointRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe()
        );
        if (recentSwingPoint.isPresent()) {
            SwingPoint recent = recentSwingPoint.get();
            if (!recent.getConfirmed()) {
                if (SwingType.valueOf(recent.getSwingType()) == SwingType.HIGH) {
                    if (priceMovedEnough(candleEntity, recent, false)) {
                        recent.setConfirmed(true);
                        swingPointRepository.save(recent);
                    }
                } else {
                    if (priceMovedEnough(candleEntity, recent, true)) {
                        recent.setConfirmed(true);
                        swingPointRepository.save(recent);
                    }
                }
            }
        }


        List<SwingPoint> swingPoints = swingPointRepository.findTop2ByStockSymbolAndTimeframeAndConfirmedTrueOrderByCandleTimestampDesc(candleEntity.getStockSymbol(), candleEntity.getTimeframe())
                .stream().sorted(Comparator.comparing(SwingPoint::getCandleTimestamp)).toList();
        if (swingPoints.size() < 2) {
            return;
        }

        SwingType lastSwingType = SwingType.valueOf(swingPoints.get(1).getSwingType());
        SwingPoint weakSwingPoint = swingPoints.get(0);

        if (lastSwingType == SwingType.HIGH) {
            breakOfStructure = priceMovedEnough(candleEntity, weakSwingPoint, false);
        } else if (lastSwingType == SwingType.LOW) {
            breakOfStructure = priceMovedEnough(candleEntity, weakSwingPoint, true);
        }

        boolean bosExists = breakOfStructureRepository.existsByWeakSwingPointAndStrongSwingPoint(weakSwingPoint, swingPoints.get(1));
        if (bosExists) {
            return;
        }

        if (breakOfStructure) {
            SwingPoint strongSwingPoint = swingPoints.get(1);
            strongSwingPoint.setIsMajor(true);
            swingPointRepository.save(strongSwingPoint);
            breakOfStructureRepository.save(
                    new BreakOfStructure(
                        candleEntity.getStockSymbol(),
                        candleEntity.getTimeframe(),
                        candleEntity.getCandleTimestamp(),
                        lastSwingType == SwingType.HIGH ? "BULLISH" : "BEARISH",
                        lastSwingType == SwingType.HIGH ? "LOW" : "HIGH",
                        weakSwingPoint,
                        swingPoints.get(1)
                    )
            );
            bosEventProducer.sendBOSEvent(
                    new BOSEvent(
                        candleEntity.getStockSymbol(),
                        candleEntity.getTimeframe(),
                        lastSwingType == SwingType.HIGH ? "BULLISH" : "BEARISH",
                        candleEntity.getCandleTimestamp(),
                        weakSwingPoint
                    )
            );
        }
    }

    public double getBOSThreshold(String timeframe) {
        String key = "bos.threshold." + timeframe;
        String value = env.getProperty(key);
        if (value == null) throw new IllegalArgumentException("No BOS threshold configured for timeframe: " + timeframe);
        return Double.parseDouble(value);
    }

    boolean priceMovedEnough(CandleEntity candleEntity, SwingPoint swingPoint, boolean direction) {
        double priceChange;
        if (direction) {
            priceChange = (candleEntity.getHigh() - swingPoint.getPrice()) * 100 / swingPoint.getPrice();
        } else {
            priceChange = (swingPoint.getPrice() - candleEntity.getLow()) * 100 / swingPoint.getPrice();
        }
        return priceChange >= getBOSThreshold(candleEntity.getTimeframe());
    }
}
