package com.market.analysis.technical.service;

import com.market.analysis.technical.model.DemandZone;
import com.market.analysis.technical.model.SupplyZone;
import com.market.common.SwingPoint;
import com.market.external.polygon.dto.Candle;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ZoneDetectionService {

    public List<DemandZone> detectDemandZones(List<Candle> candles, List<SwingPoint> swingPoints) {
        return Collections.emptyList();
    }

    public List<SupplyZone> detectSupplyZones(List<Candle> candles, List<SwingPoint> swingPoints) {
        return Collections.emptyList();
    }
}
