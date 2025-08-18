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
import java.util.Objects;
import java.util.Optional;

import static java.lang.Math.abs;

@Service
public class CandleDataAggregationService {

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

        MarketIndicators marketIndicators = marketIndicatorsRepository.findByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (marketIndicators == null) return;

        // Set basic candle data
        data.setStockSymbol(stockSymbol);
        data.setTimeframe(timeframe);
        data.setCandleTimestamp(candleTimestamp);

        setZoneData(data, stockSymbol, timeframe, candleTimestamp, trade, marketIndicators);

        // Get last liquidity sweep type
//        setLiquiditySweepDirection(data, stockSymbol, timeframe);

        // Get BOS direction
//        setBosDirection(data, stockSymbol, timeframe, marketIndicators);

        // Get buy/sell liquidity levels
//        setLiquidityLevels(data, stockSymbol, timeframe, trade, marketIndicators);

        // Get volatility
        setIndicators(data, marketIndicators);

        // Trade-related fields will be filled later
        data.setTrade(trade.getTradeType()); // Will be set during trade processing
        data.setEntryPrice(trade.getEntryPrice());
        data.setResilience(trade.getZone().getResilience());
        data.setHalfLife(trade.getZone().getHalfLife());
        data.setTimeToReturn(trade.getTimeToReturn());
        data.setSameDirectionMaxMove(trade.getZone().getSameDirectionExtremeMovement());
        data.setTradeResult("NA");

        candleAggregatedDataRepository.save(data);
    }

    private void setZoneData(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, LocalDateTime candleTimestamp, Trade trade, MarketIndicators marketIndicators) {
        LocalDateTime oneMonthAgoTimestamp = candleTimestamp.minusMonths(1);
        String oppositeZoneType = Objects.equals(trade.getZone().getZoneType(), "DEMAND") ? "SUPPLY" : "DEMAND";
        Optional<Zone> opposingZone = zoneRepository.findLatestZoneByTypeActiveOrValid(stockSymbol, timeframe, oppositeZoneType, candleTimestamp, oneMonthAgoTimestamp);

        if (opposingZone.isPresent()) {
            Zone zone = opposingZone.get();
            Double price = zone.getFarPoint();
            Double entryPrice = trade.getEntryPrice();
            Double percentMove = (entryPrice - price)*100/entryPrice;
            percentMove /= marketIndicators.getVolatility200();

            if (Objects.equals(trade.getTradeType(), "BUY")) {
                percentMove *= -1;
            }

            data.setOpposingZoneDistance(percentMove);
            data.setOpposingZoneVolume(zone.getVolume()/marketIndicators.getVolume200());
            data.setOpposingZoneStrength(zone.getStrength());
        } else {
            data.setOpposingZoneDistance(10);
            data.setOpposingZoneVolume(0);
            data.setOpposingZoneStrength(0);
        }

        data.setSameZoneStrength(trade.getZone().getStrength() / marketIndicators.getVolatility200());
        data.setSameZoneVolume(trade.getZone().getVolume() / marketIndicators.getVolume200());
        data.setZoneTaps(trade.getZone().getNoOfTaps());

    }

    private void setLiquiditySweepDirection(CandleAggregatedDataEntity data, String stockSymbol, String timeframe) {
        Optional<LiquiditySweep> lastLiquiditySweep = liquiditySweepRepository.findLatestByStockSymbolAndTimeframe(stockSymbol, timeframe);
        if (lastLiquiditySweep.isPresent()) {
            data.setLiquiditySweepDirection(lastLiquiditySweep.get().getSweepType());
        } else {
            data.setLiquiditySweepDirection("NA");
        }
    }

    private void setBosDirection(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, MarketIndicators marketIndicators) {
        Optional<BreakOfStructure> bos = breakOfStructureRepository.findTopByStockSymbolAndTimeframeOrderByCandleTimestampDesc(stockSymbol, timeframe);
        if (bos.isPresent()) {
            data.setBosDirection(bos.get().getDirection());
            data.setBosVolume(bos.get().getBosVolume() / marketIndicators.getVolume200());
        } else {
            data.setBosDirection("NA");
            data.setBosVolume(0.0);
        }
    }

    private void setLiquidityLevels(CandleAggregatedDataEntity data, String stockSymbol, String timeframe, Trade trade, MarketIndicators marketIndicators) {
        Optional<Liquidity> sameLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            stockSymbol, timeframe, trade.getTradeType().equals("BUY") ? "BUY_SWEEP" : "SELL_SWEEP");
        Optional<Liquidity> oppositeLiquidity = liquidityRepository.findFirstByStockSymbolAndTimeframeAndLiquidityTypeOrderByCandleTimestampDesc(
            stockSymbol, timeframe, trade.getTradeType().equals("SELL") ? "BUY_SWEEP" : "SELL_SWEEP");

        Double entryPrice = trade.getEntryPrice();

        if (sameLiquidity.isPresent()) {
            double price = sameLiquidity.get().getPrice();
            double percMove = (entryPrice - price)*100 / entryPrice;

            if (Objects.equals(sameLiquidity.get().getLiquidityType(), "SELL_SWEEP")) {
                percMove *= -1;
            }
            percMove /= marketIndicators.getVolatility200();

            data.setSameLiquidityDistance(percMove);
        } else {
            data.setSameLiquidityDistance(10.0);
        }

        if (oppositeLiquidity.isPresent()) {
            double price = oppositeLiquidity.get().getPrice();
            double percMove = (entryPrice - price)*100 / entryPrice;
            percMove /= marketIndicators.getVolatility200();

            if (Objects.equals(oppositeLiquidity.get().getLiquidityType(), "SELL_SWEEP")) {
                percMove *= -1;
            }

            data.setOpposingLiquidityDistance(percMove);
        } else {
            data.setOpposingLiquidityDistance(10.0);
        }
    }

    private void setIndicators(CandleAggregatedDataEntity data, MarketIndicators marketIndicators) {
//        data.setVolatility14(marketIndicators.getVolatility14());
//        data.setVolatility50(marketIndicators.getVolatility50());
        data.setVolatility200(marketIndicators.getVolatility200());
        if (marketIndicators.getRsi14() != null) data.setRsi14(marketIndicators.getRsi14());
        if (marketIndicators.getRsi50() != null) data.setRsi50(marketIndicators.getRsi50());
//        data.setVolume14(marketIndicators.getVolume14());
//        data.setVolume50(marketIndicators.getVolume50());
//        data.setVolume200(marketIndicators.getVolume200());
    }
}
