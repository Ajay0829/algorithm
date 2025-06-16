package com.market.external.polygon.dto;

import java.util.List;

public class PolygonAggregatesResponse {
    private String ticker;
    private int queryCount;
    private int resultsCount;
    private List<Candle> results;
    private String status;

    // getters and setters
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public int getQueryCount() { return queryCount; }
    public void setQueryCount(int queryCount) { this.queryCount = queryCount; }
    public int getResultsCount() { return resultsCount; }
    public void setResultsCount(int resultsCount) { this.resultsCount = resultsCount; }
    public List<Candle> getResults() { return results; }
    public void setResults(List<Candle> results) { this.results = results; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}