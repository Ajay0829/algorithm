package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.LiquiditySweep;
import com.market.streamline.entity.SwingPoint;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LiquidityService {

    public Optional<LiquiditySweep> checkLiquiditySweep(SwingPoint swingPoint) {
        // Logic to check for liquidity sweep based on the swing point
        // Assert that there was no BOS before this swing point
        return Optional.empty();
    }

    public void updateLiquidityZones(SwingPoint swingPoint) {
        // Logic to update liquidity zones based on the swing point
    }

    public void invalidateLiquidityZones(CandleEntity candleEntity) {
        // Logic to invalidate liquidity zones based on the candle entity
    }
}
