package com.market.streamline.aggregation;

import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import com.market.streamline.entity.liquidity.Liquidity;
import com.market.streamline.entity.liquidity.LiquiditySweep;
import com.market.streamline.entity.structure.*;
import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CandleDataAggregationService {

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
    private MarketIndicatorsRepository marketIndicatorsRepository;
    @Autowired
    private CandleAggregatedDataRepository candleAggregatedDataRepository;


    public void saveAggregatedData(CandleEntity candle, Trade trade) {
        LocalDateTime candleTimestamp = candle.getCandleTimestamp();
        String stockSymbol = candle.getStockSymbol();
        String timeframe = candle.getTimeframe();

        CandleAggregatedDataEntity data = new CandleAggregatedDataEntity();

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
        setIndicators(data, stockSymbol, timeframe, candleTimestamp);

        // Trade-related fields will be filled later
        data.setTrade(trade.getTradeType()); // Will be set during trade processing
        data.setEntryPrice(trade.getEntryPrice());
        data.setTimeToReturn(trade.getTimeToReturn());
        data.setTradeResult("NA");

        candleAggregatedDataRepository.save(data);
    }

    private void setLastSwingPoints(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
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

    private void setSupplyDemandPrices(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<Zone> demandZone = zoneRepository.findLatestZoneByTypeActiveOrValid(stockSymbol, timeframe, "DEMAND", candleTimestamp);
        Optional<Zone> supplyZone = zoneRepository.findLatestZoneByTypeActiveOrValid(stockSymbol, timeframe, "SUPPLY", candleTimestamp);

        demandZone.ifPresent(zone -> {
            data.setDemandPrice(zone.getNearPoint());
            data.setDemandVolume(zone.getVolume());
        });
        supplyZone.ifPresent(zone -> {
            data.setSupplyPrice(zone.getNearPoint());
            data.setSupplyVolume(zone.getVolume());
        });
    }

    private void setLastLiquiditySweepType(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<LiquiditySweep> lastLiquiditySweep = liquiditySweepRepository.findLatestByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (lastLiquiditySweep.isPresent()) {
            data.setLastLiquiditySweepType(lastLiquiditySweep.get().getSweepType());
        } else {
            data.setLastLiquiditySweepType("NA");
        }
    }

    private void setBosDirection(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<BreakOfStructure> bos = breakOfStructureRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(stockSymbol, timeframe);
        if (bos.isPresent()) {
            data.setBosDirection(bos.get().getDirection());
            data.setBosVolume(bos.get().getBosVolume());
        } else {
            data.setBosDirection("NA");
            data.setBosVolume(0.0);
        }
    }

    private void setLiquidityLevels(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        Optional<Liquidity> buyLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            stockSymbol, timeframe, "BUY_SWEEP");
        Optional<Liquidity> sellLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            stockSymbol, timeframe, "SELL_SWEEP");

        buyLiquidity.ifPresent(liquidity -> {
            data.setBuyLiquidity(liquidity.getPrice());
            data.setBuyLiquidityStrength(liquidity.getStrength());
        });
        sellLiquidity.ifPresent(liquidity -> {
            data.setSellLiquidity(liquidity.getPrice());
            data.setSellLiquidityStrength(liquidity.getStrength());
        });
    }

    private void setIndicators(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp) {
        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (marketIndicators == null) {
            data.setVolatility(0);
            data.setRsi14(0);
            data.setAverageVolume(0);
            return;
        }
        data.setVolatility(marketIndicators.getVolatility());
        if (marketIndicators.getRsi14() != null) data.setRsi14(marketIndicators.getRsi14());
        data.setAverageVolume(marketIndicators.getAverageVolume());
    }
}
