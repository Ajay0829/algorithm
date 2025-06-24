package com.market.external.polygon.service;

import com.market.external.polygon.dto.Candle;
import com.market.external.polygon.dto.PolygonAggregatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PolygonService {
    private final RestTemplate restTemplate;

    @Value("${polygon.api.key}")
    private String apiKey;

    public PolygonService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Candle> getAggregates(String ticker, String timeframe, String from, String to) {

        Pair<String, String> multiplierAndTimespan = getMultiplierAndTimespan(timeframe);

        PolygonAggregatesResponse response = getResponse(ticker, from, to, multiplierAndTimespan.getFirst(), multiplierAndTimespan.getSecond());

        return response.getResults();

    }

    public PolygonAggregatesResponse getResponse(String ticker, String from, String to, String multiplier, String timespan) {
        String url = String.format(
            "https://api.polygon.io/v2/aggs/ticker/%s/range/%s/%s/%s/%s?apiKey=%s&limit=50000",
            ticker, multiplier, timespan, from, to, apiKey
        );
        List<com.market.external.polygon.dto.Candle> allResults = new ArrayList<>();
        PolygonAggregatesResponse response = restTemplate.getForObject(url, PolygonAggregatesResponse.class);
        if (response != null && response.getResults() != null) {
            allResults.addAll(response.getResults());
        }
        String nextUrl = response != null ? response.getNext_url() : null;
        while (nextUrl != null && !nextUrl.isEmpty()) {
            // Polygon's next_url does not include the apiKey, so we need to append it if missing
            String pagedUrl = nextUrl.contains("apiKey=") ? nextUrl : nextUrl + "&apiKey=" + apiKey;
            PolygonAggregatesResponse nextResponse = restTemplate.getForObject(pagedUrl, PolygonAggregatesResponse.class);
            if (nextResponse != null && nextResponse.getResults() != null) {
                allResults.addAll(nextResponse.getResults());
            }
            nextUrl = nextResponse != null ? nextResponse.getNext_url() : null;
        }
        if (response != null) {
            response.setResults(allResults);
            response.setResultsCount(allResults.size());
        }
        return response;
    }

    Pair<String, String> getMultiplierAndTimespan(String timeframe) {
        String multiplier;
        String timespan;
        if (Objects.equals(timeframe, "15m")) {
            multiplier = "15";
            timespan = "minute";
        } else if (Objects.equals(timeframe, "1d")) {
            multiplier = "1";
            timespan = "day";
        } else if (Objects.equals(timeframe, "1h")) {
            multiplier = "1";
            timespan = "hour";
        } else {
            throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }
        return Pair.of(multiplier, timespan);
    }
}


// select * from swing_points where timeframe = '15m' and candle_timestamp >= '2025-04-05 00:00:00' and candle_timestamp <= '2025-04-12 23:59:59' order by candle_timestamp asc;

