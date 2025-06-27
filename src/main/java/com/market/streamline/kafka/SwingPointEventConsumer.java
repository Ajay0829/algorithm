package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.dto.ChartSwingDTO;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.model.SwingPointEvent;
import com.market.streamline.service.ImpulseZoneService;
import com.market.streamline.service.LiquidityService;
import com.market.streamline.service.TrendService;
import com.market.streamline.service.ZoneService;
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

            if (swingPoint.getIsMajor()) {
                impulseZoneService.detectHTFZone(swingPoint);
            }

            String type = swingPoint.getSwingType().equals("HIGH") ? "high" : "low";
            String imp = swingPoint.getIsMajor() ? "major_" : "minor_";
            chartAnnotationProducer.sendAnnotation(
                    new ChartSwingDTO(
                            "swing",
                            "created",
                            new ChartSwingDTO.SwingData(
                                    imp + type,
                                    swingPoint.getCandleTimestamp().toString(),
                                    swingPoint.getSwingType().equals("HIGH"),
                                    swingPoint.getTimeframe()

                            )
                    )
            );

            // check if the swing point is liquidity sweep
            liquidityService.checkLiquiditySweep(swingPoint);

            // update any liquidity zones based on the swing point
            liquidityService.updateLiquidityZones(swingPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
