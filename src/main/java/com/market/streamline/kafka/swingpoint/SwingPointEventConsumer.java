package com.market.streamline.kafka.swingpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.entity.structure.SwingPoint;
import com.market.streamline.kafka.model.SwingPointEvent;
import com.market.streamline.plot.kafka.ChartAnnotationConsumer;
import com.market.streamline.plot.kafka.ChartAnnotationProducer;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SwingPointEventConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TrendService trendService;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private LiquidityService liquidityService;
    @Autowired
    private ImpulseZoneService impulseZoneService;
    @Autowired
    private ChartAnnotationConsumer chartAnnotationConsumer;
    @Autowired
    private ChartAnnotationProducer chartAnnotationProducer;
    @Autowired
    private ChartAnnotationService chartAnnotationService;

    @KafkaListener(topics = "swing-point-event-topic", groupId = "swing-point-event-group")
    public void listen(String message) {
        try {
            SwingPointEvent swingPointEvent = objectMapper.readValue(message, SwingPointEvent.class);

            SwingPoint swingPoint = new SwingPoint(
                    swingPointEvent.getStockSymbol(),
                    swingPointEvent.getTimeframe(),
                    LocalDateTime.parse(swingPointEvent.getCandleTimestamp()),
                    swingPointEvent.getSwingType(),
                    swingPointEvent.getPrice(),
                    swingPointEvent.isConfirmed(),
                    swingPointEvent.isMajor()
            );

            // Handle confirmed swing points specifically
            if (swingPoint.getConfirmed()) {
                handleConfirmedSwingPoint(swingPoint);
            }

            // Immediately after I get a first major swing point ( 5 window inflection )
            if (swingPoint.getIsMajor() && !swingPoint.getConfirmed()) {
                impulseZoneService.detectHTFZone(swingPoint);
            }

            chartAnnotationService.processSwingPoint(swingPoint, "created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleConfirmedSwingPoint(SwingPoint confirmedSwingPoint) {
        liquidityService.checkLiquiditySweep(confirmedSwingPoint);
    }
}
