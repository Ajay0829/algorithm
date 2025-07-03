package com.market.streamline.plot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ChartAnnotationConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "chart-annotations", groupId = "chart-annotation-group")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            String json = record.value();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

