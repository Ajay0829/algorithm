package com.market.streamline.service;

import com.market.streamline.entity.liquidity.Liquidity;
import com.market.streamline.entity.liquidity.LiquiditySweep;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.SwingPoint;
import com.market.streamline.entity.structure.Volatility;
import com.market.streamline.plot.kafka.ChartAnnotationProducer;
import com.market.streamline.plot.ChartAnnotationService;
import com.market.streamline.repository.LiquidityRepository;
import com.market.streamline.repository.LiquiditySweepRepository;
import com.market.streamline.repository.VolatilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LiquidityService {

    private final LiquiditySweepRepository liquiditySweepRepository;
    private final LiquidityRepository liquidityRepository;
    private final VolatilityRepository volatilityRepository;
    private final ChartAnnotationProducer chartAnnotationProducer;
    private final ChartAnnotationService chartAnnotationService;

    public LiquidityService(LiquiditySweepRepository liquiditySweepRepository, LiquidityRepository liquidityRepository, VolatilityRepository volatilityRepository, ChartAnnotationProducer chartAnnotationProducer, ChartAnnotationService chartAnnotationService) {
        this.liquiditySweepRepository = liquiditySweepRepository;
        this.liquidityRepository = liquidityRepository;
        this.volatilityRepository = volatilityRepository;
        this.chartAnnotationProducer = chartAnnotationProducer;
        this.chartAnnotationService = chartAnnotationService;
    }

    public void checkLiquiditySweep(SwingPoint swingPoint) {
        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(
                swingPoint.getStockSymbol(),
                swingPoint.getTimeframe()
        );

        if (volatility == null) {
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
                    getLiquidityPrice(swingPoint, volatility.getVolatility()),
                    1
            );
            liquidityRepository.save(liquidity);

            // Send chart annotation for new liquidity zone
            sendLiquidityChartEvent(liquidity, "created");
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
                    getLiquidityPrice(swingPoint, volatility.getVolatility()),
                    liquidity.getStrength()
            );
            liquiditySweepRepository.save(liquiditySweep);

            // TODO: Update losing trades associated with this liquidity to be liquidity_sweep.

            // Send chart annotation for liquidity deletion (swept)
            sendLiquidityChartEvent(liquidity, "swept");
            liquidityRepository.delete(liquidity);
        } else {
            handleAddOrUpdateLiquidityZone(swingPoint, liquidity, volatility);
        }
    }

    private void handleAddOrUpdateLiquidityZone(SwingPoint swingPoint, Liquidity liquidity, Volatility volatility) {
        // No liquidity sweep, check to reuse or add a new liquidity zone
        if (shouldAddNewLiquidityZone(swingPoint, liquidity, volatility)) {
            // No tap and price is far from the last liquidity zone
            Liquidity liquidityZone = new Liquidity(
                    swingPoint.getStockSymbol(),
                    swingPoint.getTimeframe(),
                    swingPoint.getCandleTimestamp(),
                    getSweepType(swingPoint),
                    getLiquidityPrice(swingPoint, volatility.getVolatility()),
                    1
            );
            liquidityRepository.save(liquidityZone);

            // Send chart annotation for new liquidity zone
            sendLiquidityChartEvent(liquidityZone, "created");
        } else {
            // No Tap but price is nearby the old liquidity zone
            // increase the strength of the last liquidity zone
            liquidity.setStrength(liquidity.getStrength() + 1);
            // Liquidity pointing to latest swing point.
            // liquidity.setCandleTimestamp(swingPoint.getCandleTimestamp());
            // double liquidityPrice = getBestLiquidityPrice(swingPoint, liquidity, volatility.getVolatility());
            // liquidity.setPrice(liquidityPrice);
            liquidityRepository.save(liquidity);

            // Send chart annotation for updated liquidity zone
            sendLiquidityChartEvent(liquidity, "updated");
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

    private double getBestLiquidityPrice(SwingPoint swingPoint, Liquidity liquidity, double volatility) {
        // Logic to determine the best liquidity price based on the swing point and existing liquidity
        double existingLiquidityPrice = liquidity.getPrice();

        double newLiquidityPrice = getLiquidityPrice(swingPoint, volatility);

        if (swingPoint.getSwingType().equals("HIGH")) {
            return Math.max(existingLiquidityPrice, newLiquidityPrice);
        } else {
            return Math.min(existingLiquidityPrice, newLiquidityPrice);
        }
    }

    private boolean priceTappedLastLiquidity(SwingPoint swingPoint, Liquidity liquidity) {
        if (swingPoint.getSwingType().equals("HIGH")) {
            return swingPoint.getPrice() >= liquidity.getPrice();
        } else {
            return swingPoint.getPrice() <= liquidity.getPrice();
        }
    }

    private boolean shouldAddNewLiquidityZone(SwingPoint swingPoint, Liquidity liquidity, Volatility volatility) {
        double distance = Math.abs(swingPoint.getPrice() - liquidity.getPrice());
        double percentageDistance = (distance / liquidity.getPrice()) * 100;
        return percentageDistance > 2 * volatility.getVolatility();
    }

    public void invalidateLiquidityZones(CandleEntity candleEntity, boolean isHighCheck) {
        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe()
        );

        if (volatility == null) {
            return;
        }

        // Fetch all the liquidity zones based on high or low check
        List<Liquidity> liquidities = getLiquiditiesByPriceAndType(
                candleEntity.getStockSymbol(),
                candleEntity.getTimeframe(),
                isHighCheck ? candleEntity.getHigh() : candleEntity.getLow(),
                isHighCheck
        );

        liquidities.forEach(liquidity -> {
            if (isHighCheck) {
                double price = candleEntity.getHigh();
                double liquidityPrice = liquidity.getPrice();
                if (price > liquidityPrice* (1 + volatility.getVolatility() / 100)) {
                    // Send chart annotation for liquidity deletion before deleting
                    sendLiquidityChartEvent(liquidity, "deleted");
                    liquidityRepository.delete(liquidity);
                }
            } else {
                double price = candleEntity.getLow();
                double liquidityPrice = liquidity.getPrice();
                if (price < liquidityPrice * (1 - volatility.getVolatility() / 100)) {
                    // Send chart annotation for liquidity deletion before deleting
                    sendLiquidityChartEvent(liquidity, "deleted");
                    liquidityRepository.delete(liquidity);
                }
            }
        });
    }

    public List<Liquidity> getLiquiditiesByPriceAndType(String stockSymbol, String timeframe,
                                                        Double currentPrice, boolean highCheck) {
        if (highCheck) {
            // For high check: find all SELL_SWEEP liquidity prices < current high
            return liquidityRepository.findLiquiditiesBelowPrice(
                    stockSymbol,
                    timeframe,
                    "SELL_SWEEP",
                    currentPrice
            );
        } else {
            // For low check: find all BUY_SWEEP liquidity prices > current low
            return liquidityRepository.findLiquiditiesAbovePrice(
                    stockSymbol,
                    timeframe,
                    "BUY_SWEEP",
                    currentPrice
            );
        }
    }

    /**
     * Send chart annotation event for liquidity zone changes
     */
    private void sendLiquidityChartEvent(Liquidity liquidity, String action) {
        chartAnnotationService.processLiquidity(liquidity, action);
    }
}
