package com.market.streamline.service;

import com.market.common.SwingType;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.kafka.BOSEventProducer;
import com.market.streamline.model.BOSEvent;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.SwingPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BreakOfStructureService {

    @Autowired
    private BOSEventProducer bosEventProducer;
    @Autowired
    private SwingPointRepository swingPointRepository;
    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;

    public boolean checkForBreakOfStructure(CandleEntity candleEntity) {

        boolean breakOfStructure = false;

        List<SwingPoint> swingPoints = swingPointRepository.findTop2ByStockSymbolAndTimeframeAndConfirmedTrueOrderByCandleTimestampDesc(candleEntity.getStockSymbol(), candleEntity.getTimeframe())
                .stream().sorted(Comparator.comparing(SwingPoint::getCandleTimestamp)).toList();
        if (swingPoints.size() < 2) {
            return false; // Not enough swing points to determine a break of structure
        }

        SwingType lastSwingType = SwingType.valueOf(swingPoints.get(1).getSwingType());
        SwingPoint weakSwingPoint = swingPoints.get(0);
        if (lastSwingType == SwingType.HIGH) {
            breakOfStructure = priceMovedEnough(candleEntity, weakSwingPoint, false);
        } else if (lastSwingType == SwingType.LOW) {
            breakOfStructure = priceMovedEnough(candleEntity, weakSwingPoint, true);
        }

        // Check if BOS for this weak and strong swing point already exists
        boolean bosExists = breakOfStructureRepository.existsByWeakSwingPointAndStrongSwingPoint(weakSwingPoint, swingPoints.get(1));
        if (bosExists) {
            return false;
        }

        if (breakOfStructure) {
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
        return breakOfStructure;
    }

    boolean priceMovedEnough(CandleEntity candleEntity, SwingPoint swingPoint, boolean direction) {
        double priceChange;
        if (direction) {
            priceChange = (candleEntity.getHigh() - swingPoint.getPrice()) * 100 / swingPoint.getPrice();
        } else {
            priceChange = (swingPoint.getPrice() - candleEntity.getLow()) * 100 / swingPoint.getPrice();
        }
        return priceChange >= 5;
    }
}
