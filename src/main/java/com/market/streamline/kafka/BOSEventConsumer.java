package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Autowired
    private TrendService trendService;

    @KafkaListener(topics = "bos-event-topic", groupId = "bos-event-group")
    public void listen(String message) {
        try {

            BOSEvent bosEvent = objectMapper.readValue(message, BOSEvent.class);
            BreakOfStructure breakOfStructure = new BreakOfStructure();
            // Process the BOSEvent to identify zones
            zoneService.identifyZone(breakOfStructure);
            trendService.updateTrend(breakOfStructure);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

