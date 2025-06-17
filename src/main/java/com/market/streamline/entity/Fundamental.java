package com.market.streamline.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fundamentals", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_symbol", "timeframe", "candle_timestamp"})
})
public class Fundamental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_symbol", nullable = false)
    private String stockSymbol;

    @Column(name = "pe_forward")
    private Double peForward;

    @Column(name = "atr")
    private Double atr;

    @Column(name = "earnings_release_session")
    private String earningsReleaseSession;

    @Column(name = "next_earnings_date")
    private LocalDateTime nextEarningsDate;
}

