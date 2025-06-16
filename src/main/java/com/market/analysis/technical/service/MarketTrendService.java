package com.market.analysis.technical.service;

import com.market.analysis.technical.model.MarketTrend;
import com.market.common.SwingPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketTrendService {

    public MarketTrend getMarketTrend(List<SwingPoint> swingPoints) {
        return MarketTrend.CONSOLIDATION;
    }
}
