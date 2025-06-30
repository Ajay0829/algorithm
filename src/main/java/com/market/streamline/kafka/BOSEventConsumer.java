package com.market.streamline.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.model.BOSEvent;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.service.ImpulseZoneService;
import com.market.streamline.service.TrendService;
import com.market.streamline.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BOSEventConsumer {

    @Autowired
    private ZoneService zoneService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Autowired
    private TrendService trendService;

    @Autowired
    private ImpulseZoneService impulseZoneService;
    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;

    @KafkaListener(topics = "bos-event-topic", groupId = "bos-event-group")
    public void listen(String message) {
        try {
            BOSEvent bosEvent = objectMapper.readValue(message, BOSEvent.class);
            Optional<BreakOfStructure> bos = breakOfStructureRepository.
                    findByStockSymbolAndTimeframeAndCandleTimestampAndDirection(
                        bosEvent.getStockSymbol(),
                        bosEvent.getTimeframe(),
                        bosEvent.getCandleTimestamp(),
                        bosEvent.getDirection()
                    );

            if (bos.isEmpty()) {
                System.out.println("No BreakOfStructure found for the given parameters: " + bosEvent);
                return;
            }
            BreakOfStructure breakOfStructure = bos.get();
            // trendService.updateTrend(breakOfStructure);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

