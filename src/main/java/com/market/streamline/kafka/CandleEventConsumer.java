package com.market.streamline.kafka;

import com.market.external.polygon.dto.Candle;
import com.market.streamline.service.FeatureExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

@Component
public class CandleEventConsumer {

    @Autowired
    private FeatureExtractionService featureExtractionService;

    // Example: expects JSON string with keys: stockSymbol, timeframe, candleTimestamp, momentum, trend
    @KafkaListener(topics = "candle-events", groupId = "feature-extractor-group")
    public void listen(String message) {
        // Parse the message (assume JSON for this example)
        Map<String, Object> event = parseJson(message);

        // Call the service to process and upsert features
        featureExtractionService.processCandleEvent(event);
    }

    // Dummy JSON parser for illustration; replace with Jackson or Gson in production
    private Map<String, Object> parseJson(String json) {
        // ... implement or use a real JSON parser ...
        return Collections.emptyMap();
    }
}

