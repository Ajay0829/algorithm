package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.StockFeature;
import org.springframework.stereotype.Service;

@Service
public class FeatureExtractionService {

    public StockFeature extractStockFeatures(CandleEntity candleEntity) {
        // Logic to extract stock features from the candle entity after all the operations are done.
        return new StockFeature();
    }

}

