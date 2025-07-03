package com.market.streamline.plot.charts;

public class TradeData {
    private Long id;
    private String stockSymbol;
    private String timeframe;
    private String entryTimestamp;
    private Double entryPrice;
    private Double stopLoss;
    private Double takeProfit;
    private String tradeType; // "BUY", "SELL"
    private String result; // "WIN", "LOSS", "PENDING"
    private Boolean isActive;
    private String exitTimestamp;
    private Double exitPrice;

    public TradeData() {}

    public TradeData(Long id, String stockSymbol, String timeframe, String entryTimestamp,
                     Double entryPrice, Double stopLoss, Double takeProfit, String tradeType) {
        this.id = id;
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.entryTimestamp = entryTimestamp;
        this.entryPrice = entryPrice;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.tradeType = tradeType;
        this.result = "PENDING";
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public String getEntryTimestamp() { return entryTimestamp; }
    public void setEntryTimestamp(String entryTimestamp) { this.entryTimestamp = entryTimestamp; }

    public Double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(Double entryPrice) { this.entryPrice = entryPrice; }

    public Double getStopLoss() { return stopLoss; }
    public void setStopLoss(Double stopLoss) { this.stopLoss = stopLoss; }

    public Double getTakeProfit() { return takeProfit; }
    public void setTakeProfit(Double takeProfit) { this.takeProfit = takeProfit; }

    public String getTradeType() { return tradeType; }
    public void setTradeType(String tradeType) { this.tradeType = tradeType; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getExitTimestamp() { return exitTimestamp; }
    public void setExitTimestamp(String exitTimestamp) { this.exitTimestamp = exitTimestamp; }

    public Double getExitPrice() { return exitPrice; }
    public void setExitPrice(Double exitPrice) { this.exitPrice = exitPrice; }
}