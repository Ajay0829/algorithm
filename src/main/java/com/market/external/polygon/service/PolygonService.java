package com.market.external.polygon.service;

import com.market.external.polygon.dto.PolygonAggregatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class PolygonService {
    private final RestTemplate restTemplate;

    @Value("${polygon.api.key}")
    private String apiKey;

    public PolygonService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PolygonAggregatesResponse getAggregates(String ticker, String timeframe, String from, String to) {
        String multiplier;
        String timespan;
        if (Objects.equals(timeframe, "15m")) {
            multiplier = "15";
            timespan = "minute";
        } else  if (Objects.equals(timeframe, "1d")) {
            multiplier = "1";
            timespan = "day";
        } else if (Objects.equals(timeframe, "1h")) {
            multiplier = "1";
            timespan = "hour";
        } else {
            throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
        String url = String.format(
            "https://api.polygon.io/v2/aggs/ticker/%s/range/%s/%s/%s/%s?apiKey=%s&limit=50000",
            ticker, multiplier, timespan, from, to, apiKey
        );
        return restTemplate.getForObject(url, PolygonAggregatesResponse.class);
    }
}