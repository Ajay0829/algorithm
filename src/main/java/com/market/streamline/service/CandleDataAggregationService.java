package com.market.streamline.service;

import com.market.streamline.entity.*;
import com.market.streamline.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CandleDataAggregationService {

    @Autowired
    private CandleRepository candleRepository;

    @Autowired
    private SwingPointRepository swingPointRepository;

    @Autowired
    private BreakOfStructureRepository breakOfStructureRepository;

    @Autowired
    private LiquidityRepository liquidityRepository;

    @Autowired
    private LiquiditySweepRepository liquiditySweepRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private TrendRepository trendRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private VolatilityRepository volatilityRepository;

    @Autowired
    private StockFeatureRepository stockFeatureRepository;

    /**
     * Generate initial aggregated data for a candle (excluding trade information)
     */
    public CandleAggregatedData generateInitialAggregatedData(String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        CandleAggregatedData data = new CandleAggregatedData();

        // Get the candle data
        Optional<CandleEntity> candleOpt = candleRepository.findByStockSymbolAndTimeframeAndCandleTimestamp(
            stockSymbol, timeframe, candleTimestamp);

        if (candleOpt.isEmpty()) {
            return null;
        }

        CandleEntity candle = candleOpt.get();

        // Set basic candle data
        data.setStockSymbol(stockSymbol);
        data.setTimeframe(timeframe);
        data.setCandleTimestamp(candleTimestamp);
        data.setOpen(candle.getOpen());
        data.setClose(candle.getClose());
        data.setHigh(candle.getHigh());
        data.setLow(candle.getLow());
        data.setVolume(candle.getVolume());

        // Get last swing high and low
        setLastSwingPoints(data, stockSymbol, timeframe, candleTimestamp);

        // Get supply and demand prices from zones
        setSupplyDemandPrices(data, stockSymbol, timeframe, candleTimestamp);

        // Get last liquidity sweep type
        setLastLiquiditySweepType(data, stockSymbol, timeframe, candleTimestamp);

        // Get BOS direction
        setBosDirection(data, stockSymbol, timeframe, candleTimestamp);

        // Get buy/sell liquidity levels
        setLiquidityLevels(data, stockSymbol, timeframe, candleTimestamp);

        // Get volatility
        setVolatility(data, stockSymbol, timeframe, candleTimestamp);

        // Trade-related fields will be filled later
        data.setTrade(""); // Will be set during trade processing
        data.setEntryPrice(0.0);
        data.setTargetPrice(0.0);
        data.setStopLossPrice(0.0);
        data.setTradeResult(false);

        return data;
    }

    /**
     * Fill trade-related information for aggregated data
     */
    public List<CandleAggregatedData> fillTradeInformation(List<CandleAggregatedData> data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        String stockSymbol = data.get(0).getStockSymbol();
        String timeframe = data.get(0).getTimeframe();

        data.forEach(singleCandle -> {
            Optional<Trade> trade = tradeRepository.findFirstByStockSymbolAndTimeframeAndTimestamp(
                    stockSymbol, timeframe, singleCandle.getCandleTimestamp()
            );

            if (trade.isPresent()) {
                singleCandle.setTrade(trade.get().getTradeType());
                singleCandle.setTargetPrice(trade.get().getTakeProfit());
                singleCandle.setStopLossPrice(trade.get().getStopLoss());
                singleCandle.setEntryPrice(trade.get().getEntryPrice());
                singleCandle.setTradeResult(Objects.equals(trade.get().getResult(), "WIN"));
            }
        });
        return data;
    }

    private void setLastSwingPoints(CandleAggregatedData data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        List<SwingPoint> swingPoints = swingPointRepository.findTop2ByStockSymbolAndTimeframeAndConfirmedTrueOrderByCandleTimestampDescIdDesc(stockSymbol, timeframe)
                .stream().sorted(Comparator.comparing(SwingPoint::getCandleTimestamp)
                        .thenComparing(SwingPoint::getId)).toList();

        for (SwingPoint swingPoint : swingPoints) {
            if (swingPoint.getSwingType().equals("HIGH")) {
                data.setLastSwingHigh(swingPoint.getPrice());
            } else {
                data.setLastSwingLow(swingPoint.getPrice());
            }
        }

    }

    private void setSupplyDemandPrices(CandleAggregatedData data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<Zone> demandZone = zoneRepository.findLatestZoneByType(stockSymbol, timeframe, "DEMAND", candleTimestamp);
        Optional<Zone> supplyZone = zoneRepository.findLatestZoneByType(stockSymbol, timeframe, "SUPPLY", candleTimestamp);

        demandZone.ifPresent(zone -> data.setDemandPrice(zone.getNearPoint()));
        supplyZone.ifPresent(zone -> data.setSupplyPrice(zone.getNearPoint()));
    }

    private void setLastLiquiditySweepType(CandleAggregatedData data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<LiquiditySweep> lastLiquiditySweep = liquiditySweepRepository.findLatestByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (lastLiquiditySweep.isPresent()) {
            data.setLastLiquiditySweepType(Objects.equals(lastLiquiditySweep.get().getSweepType(), "BUY_SWEEP") ? 1 : 0);
        } else {
            data.setLastLiquiditySweepType(-1);
        }
    }

    private void setBosDirection(CandleAggregatedData data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<BreakOfStructure> bos = breakOfStructureRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(stockSymbol, timeframe);
        if (bos.isPresent()) {
            data.setBosDirection(Objects.equals(bos.get().getDirection(), "BULLISH") ? 1 : 0);
        } else {
            data.setBosDirection(-1);
        }
    }

    private void setLiquidityLevels(CandleAggregatedData data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<Liquidity> buyLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            stockSymbol, timeframe, "BUY_SWEEP");
        Optional<Liquidity> sellLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            stockSymbol, timeframe, "SELL_SWEEP");

        buyLiquidity.ifPresent(liquidity -> data.setBuyLiquidity(liquidity.getPrice()));
        sellLiquidity.ifPresent(liquidity -> data.setSellLiquidity(liquidity.getPrice()));
    }

    private void setVolatility(CandleAggregatedData data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (volatility != null) {
            data.setVolatility(volatility.getVolatility());
        } else {
            data.setVolatility(0.0); // Default value if no volatility data is found
        }
    }
}
