package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.model.BOSEvent;
import com.market.streamline.service.TrendService;
import com.market.streamline.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BOSEventConsumer {

    @Autowired
    private ZoneService zoneService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TrendService trendService;

    @KafkaListener(topics = "bos-event-topic", groupId = "bos-event-group")
    public void listen(String message) {
        try {

            BOSEvent bosEvent = objectMapper.readValue(message, BOSEvent.class);
            System.out.println("Received BOSEvent: " + bosEvent);

            BreakOfStructure breakOfStructure = new BreakOfStructure();
            // Process the BOSEvent to identify zones
            zoneService.identifyZone(breakOfStructure);
            trendService.updateTrend(breakOfStructure);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

