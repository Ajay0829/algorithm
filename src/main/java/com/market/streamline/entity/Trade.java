package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "entry_price", nullable = false)
    private Double entryPrice;

    @Column(name = "stop_loss", nullable = false)
    private Double stopLoss;

    @Column(name = "take_profit", nullable = false)
    private Double takeProfit;

    @Column(name = "trade_type", nullable = false)
    private String tradeType; // "BUY" or "SELL"

    @Column(name = "result")
    private String result; // Trade outcome: "WIN", "LOSS", "PENDING", etc.

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Trade() {}

    public Trade(String stockSymbol, String timeframe, LocalDateTime timestamp, Double entryPrice, Double stopLoss, Double takeProfit, String tradeType, Boolean isActive) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.timestamp = timestamp;
        this.entryPrice = entryPrice;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.tradeType = tradeType;
        this.isActive = isActive;
        this.result = "PENDING"; // Default to pending when trade is created
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

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

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
