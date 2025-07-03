package com.market.streamline.entity.structure;

import jakarta.persistence.*;

@Entity
@Table(name = "volatility")
public class Volatility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;

    @Column(name = "volatility", nullable = false)
    private Double volatility;

    public Volatility() {}

    public Volatility(String stockSymbol, String timeframe, Double volatility) {
        this.stockSymbol = stockSymbol;
        this.timeframe = timeframe;
        this.volatility = volatility;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStockSymbol() { return stockSymbol; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public Double getVolatility() { return volatility; }
    public void setVolatility(Double volatility) { this.volatility = volatility; }
}
