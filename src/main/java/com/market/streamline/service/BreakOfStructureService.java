package com.market.streamline.service;

import com.market.common.SwingType;
import com.market.streamline.dto.ChartBOSDTO;
import com.market.streamline.dto.ChartSwingDTO;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.Volatility;
import com.market.streamline.kafka.BOSEventProducer;
import com.market.streamline.kafka.ChartAnnotationProducer;
import com.market.streamline.model.BOSEvent;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.SwingPointRepository;
import com.market.streamline.repository.VolatilityRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class BreakOfStructureService {

    private final BOSEventProducer bosEventProducer;
    private final SwingPointRepository swingPointRepository;
    private final BreakOfStructureRepository breakOfStructureRepository;
    private final Environment env;
    private final VolatilityRepository volatilityRepository;
    private final ChartAnnotationProducer chartAnnotationProducer;

    public BreakOfStructureService(BOSEventProducer bosEventProducer, SwingPointRepository swingPointRepository, BreakOfStructureRepository breakOfStructureRepository, Environment env, VolatilityRepository volatilityRepository, ChartAnnotationProducer chartAnnotationProducer) {
        this.bosEventProducer = bosEventProducer;
        this.swingPointRepository = swingPointRepository;
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.env = env;
        this.volatilityRepository = volatilityRepository;
        this.chartAnnotationProducer = chartAnnotationProducer;
    }

    public void checkForBreakOfStructure(CandleEntity candleEntity, boolean isHighCheck) {

        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe()
        );
        if (volatility == null) {
            return;
        }
        boolean breakOfStructure = false;
        double volatilityValue = volatility.getVolatility();

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
            chartAnnotationProducer.sendAnnotation(
                    new ChartSwingDTO(
                            "swing",
                            "updated",
                            new ChartSwingDTO.SwingData(
                                    strongSwingPoint.getSwingType().equals("HIGH") ? "major_high" : "major_low",
                                    strongSwingPoint.getCandleTimestamp().toString(),
                                    strongSwingPoint.getSwingType().equals("HIGH"),
                                    strongSwingPoint.getTimeframe()

                            )
                    )
            );
            breakOfStructureRepository.save(
                    new BreakOfStructure(
                        candleEntity.getStockSymbol(),
                        candleEntity.getTimeframe(),
                        candleEntity.getCandleTimestamp(),
                        lastSwingType == SwingType.HIGH ? "BEARISH" : "BULLISH",
                        lastSwingType == SwingType.HIGH ? "LOW" : "HIGH",
                        weakSwingPoint,
                        strongSwingPoint
                    )
            );

            chartAnnotationProducer.sendAnnotation(
                    new ChartBOSDTO(
                            "bos",
                            "created",
                            new ChartBOSDTO.BOSData(
                                    weakSwingPoint.getCandleTimestamp().toString(),
                                    candleEntity.getCandleTimestamp().toString(),
                                    lastSwingType == SwingType.HIGH ? "BEARISH" : "BULLISH",
                                    candleEntity.getTimeframe(),
                                    weakSwingPoint.getPrice()
                            )
                    )
            );
            bosEventProducer.sendBOSEvent(
                    new BOSEvent(
                        candleEntity.getStockSymbol(),
                        candleEntity.getTimeframe(),
                        lastSwingType == SwingType.HIGH ? "BEARISH" : "BULLISH",
                        candleEntity.getCandleTimestamp(),
                        weakSwingPoint
                    )
            );
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