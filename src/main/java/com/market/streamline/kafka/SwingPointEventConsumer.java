package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.model.SwingPointEvent;
import com.market.streamline.service.LiquidityService;
import com.market.streamline.service.TrendService;
import com.market.streamline.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SwingPointEventConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TrendService trendService;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private LiquidityService liquidityService;

    @KafkaListener(topics = "swing-point-event-topic", groupId = "swing-point-event-group")
    public void listen(String message) {
        try {
            SwingPointEvent swingPointEvent = objectMapper.readValue(message, SwingPointEvent.class);
            System.out.println("Received SwingPointEvent: " + swingPointEvent);

            SwingPoint swingPoint = new SwingPoint();

            // update the strength of current trend
            trendService.updateTrendStrength(swingPoint);

            // check if the swing point is liquidity sweep
            liquidityService.checkLiquiditySweep(swingPoint);

            // update any liquidity zones based on the swing point
            liquidityService.updateLiquidityZones(swingPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

