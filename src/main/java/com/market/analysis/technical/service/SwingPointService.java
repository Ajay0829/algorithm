package com.market.analysis.technical.service;

import com.market.common.SwingPoint;
import com.market.external.polygon.dto.Candle;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SwingPointService {

    public List<SwingPoint> detectSwingPoints(List<Candle> prices) {

        return Collections.emptyList();
    }
}
