package com.market.streamline.service;

import com.market.streamline.entity.liquidity.Liquidity;
import com.market.streamline.entity.liquidity.LiquiditySweep;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.MarketIndicators;
import com.market.streamline.entity.structure.SwingPoint;
import com.market.streamline.repository.LiquidityRepository;
import com.market.streamline.repository.LiquiditySweepRepository;
import com.market.streamline.repository.MarketIndicatorsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class LiquidityService {

    private final LiquiditySweepRepository liquiditySweepRepository;
    private final LiquidityRepository liquidityRepository;
    private final MarketIndicatorsRepository marketIndicatorsRepository;

    public LiquidityService(LiquiditySweepRepository liquiditySweepRepository, LiquidityRepository liquidityRepository, MarketIndicatorsRepository marketIndicatorsRepository) {
        this.liquiditySweepRepository = liquiditySweepRepository;
        this.liquidityRepository = liquidityRepository;
        this.marketIndicatorsRepository = marketIndicatorsRepository;
    }

    public void checkLiquiditySweep(SwingPoint swingPoint) {

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(swingPoint.getStockSymbol(), swingPoint.getTimeframe());

        if (marketIndicators == null) {
            return;
        }
        // Add check to fetch only valid liquidities,
        // should we also delete swept liquidities??
        Optional<Liquidity> lastLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
                swingPoint.getStockSymbol(),
                swingPoint.getTimeframe(),
                getSweepType(swingPoint)
        );

        if (lastLiquidity.isEmpty()) {
            Liquidity liquidity = new Liquidity(
                    swingPoint.getStockSymbol(),
                    swingPoint.getTimeframe(),
                    swingPoint.getCandleTimestamp(),
                    getSweepType(swingPoint),
                    getLiquidityPrice(swingPoint, marketIndicators.getVolatility200()),
                    1
            );
            liquidityRepository.save(liquidity);
            return;
        }

        Liquidity liquidity = lastLiquidity.get();

        if (priceTappedLastLiquidity(swingPoint, liquidity)) {
            LiquiditySweep liquiditySweep = new LiquiditySweep(
                    swingPoint.getStockSymbol(),
                    swingPoint.getTimeframe(),
                    swingPoint.getCandleTimestamp(),
                    getSweepType(swingPoint),
                    0.0,
                    getLiquidityPrice(swingPoint, marketIndicators.getVolatility200()),
                    liquidity.getStrength()
            );
            if (!liquiditySweepRepository.existsByStockSymbolAndTimeframeAndCandleTimestamp(swingPoint.getStockSymbol(), swingPoint.getTimeframe(), swingPoint.getCandleTimestamp())) {
                liquiditySweepRepository.save(liquiditySweep);
            } else {
                return;
            }
            liquidityRepository.delete(liquidity);
        } else {
            handleAddOrUpdateLiquidityZone(swingPoint, liquidity, marketIndicators);
        }
    }

    private void handleAddOrUpdateLiquidityZone(SwingPoint swingPoint, Liquidity liquidity, MarketIndicators marketIndicators) {
        // No liquidity sweep, check to reuse or add a new liquidity zone
        if (shouldAddNewLiquidityZone(swingPoint, liquidity, marketIndicators)) {
            // No tap and price is far from the last liquidity zone
            Liquidity liquidityZone = new Liquidity(
                    swingPoint.getStockSymbol(),
                    swingPoint.getTimeframe(),
                    swingPoint.getCandleTimestamp(),
                    getSweepType(swingPoint),
                    getLiquidityPrice(swingPoint, marketIndicators.getVolatility200()),
                    1
            );
            liquidityRepository.save(liquidityZone);
        } else {
            // No Tap but price is nearby the old liquidity zone
            // increase the strength of the last liquidity zone
            liquidity.setStrength(liquidity.getStrength() + 1);
            liquidityRepository.save(liquidity);
        }
    }

    private String getSweepType(SwingPoint swingPoint) {
        return swingPoint.getSwingType().equals("HIGH") ? "SELL_SWEEP" : "BUY_SWEEP";
    }

    private double getLiquidityPrice(SwingPoint swingPoint, double volatility) {
        double swingPointPrice = swingPoint.getPrice();
        if (swingPoint.getSwingType().equals("HIGH")) {
            return swingPointPrice * (1 + volatility/100);
        } else {
            return swingPointPrice * (1 - volatility/100);
        }
    }

    private boolean priceTappedLastLiquidity(SwingPoint swingPoint, Liquidity liquidity) {
        if (swingPoint.getSwingType().equals("HIGH")) {
            return swingPoint.getPrice() >= liquidity.getPrice();
        } else {
            return swingPoint.getPrice() <= liquidity.getPrice();
        }
    }

    private boolean shouldAddNewLiquidityZone(SwingPoint swingPoint, Liquidity liquidity, MarketIndicators marketIndicators) {
        double distance = Math.abs(swingPoint.getPrice() - liquidity.getPrice());
        double percentageDistance = (distance / liquidity.getPrice()) * 100;
        return percentageDistance > 2 * marketIndicators.getVolatility200();
    }

    public void invalidateLiquidityZones(CandleEntity candleEntity, boolean isHighCheck) {
        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(candleEntity.getStockSymbol(), candleEntity.getTimeframe());

        if (marketIndicators == null) {
            return;
        }
        Double currentPrice = isHighCheck ? candleEntity.getHigh() : candleEntity.getLow();

        // Fetch all the liquidity zones based on high or low check
        List<Liquidity> liquidities = getLiquiditiesByPrice(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                currentPrice
        );


        liquidities.forEach(liquidity -> {
            double liquidityPrice = liquidity.getPrice();
            if (liquidity.getLiquidityType().equals("SELL_SWEEP")) {
                if (currentPrice > liquidityPrice* (1 + marketIndicators.getVolatility200() / 100)) {
                    // Send chart annotation for liquidity deletion before deleting
                    liquidityRepository.delete(liquidity);
                }
            } else {
                if (currentPrice < liquidityPrice * (1 - marketIndicators.getVolatility200() / 100)) {
                    // Send chart annotation for liquidity deletion before deleting
                    liquidityRepository.delete(liquidity);
                }
            }
        });
    }

    public List<Liquidity> getLiquiditiesByPrice(String stockSymbol, String timeframe,
                                                        Double currentPrice) {
        List<Liquidity> sellLiquidities = liquidityRepository.findLiquiditiesBelowPrice(
                    stockSymbol,
                    timeframe,
                    "SELL_SWEEP",
                    currentPrice
            );
        List<Liquidity> buyLiquidities = liquidityRepository.findLiquiditiesAbovePrice(
                    stockSymbol,
                    timeframe,
                    "BUY_SWEEP",
                    currentPrice
            );

        return Stream.concat(sellLiquidities.stream(), buyLiquidities.stream()).toList();
    }
}
