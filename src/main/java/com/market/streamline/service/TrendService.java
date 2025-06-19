package com.market.streamline.service;

import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.Trend;
import org.springframework.stereotype.Service;

@Service
public class TrendService {

    public Trend updateTrend(BreakOfStructure bos) {

        return new Trend();
    }

    public Trend updateTrendStrength(SwingPoint swingPoint) {
        return new Trend();
    }
}
