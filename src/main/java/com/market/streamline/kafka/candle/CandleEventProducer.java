package com.market.streamline.kafka.candle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.streamline.kafka.model.CandleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CandleEventProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendCandleEvent(CandleEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("candle-added", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

