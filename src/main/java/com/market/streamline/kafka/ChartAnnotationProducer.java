package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChartAnnotationProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendAnnotation(Object annotation) {
        try {
            String json = objectMapper.writeValueAsString(annotation);
            kafkaTemplate.send("chart-annotations", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

