package com.market.streamline.aggregation;

import com.market.streamline.entity.aggregation.CandleAggregatedDataEntity;
import org.springframework.stereotype.Service;

@Service
public class CandleAggregatedDataMapper {

    public CandleAggregatedDataEntity toEntity(CandleAggregatedData data) {
        CandleAggregatedDataEntity entity = new CandleAggregatedDataEntity();

        entity.setStockSymbol(data.getStockSymbol());
        entity.setTimeframe(data.getTimeframe());
        entity.setCandleTimestamp(data.getCandleTimestamp());
        entity.setOpen(data.getOpen());
        entity.setClose(data.getClose());
        entity.setHigh(data.getHigh());
        entity.setLow(data.getLow());
        entity.setVolume(data.getVolume());
        entity.setLastSwingHigh(data.getLastSwingHigh());
        entity.setLastSwingLow(data.getLastSwingLow());
        entity.setSupplyPrice(data.getSupplyPrice());
        entity.setSupplyVolume(data.getSupplyVolume());
        entity.setDemandPrice(data.getDemandPrice());
        entity.setDemandVolume(data.getDemandVolume());
        entity.setLastLiquiditySweepType(data.getLastLiquiditySweepType());
        entity.setBosDirection(data.getBosDirection());
        entity.setBosVolume(data.getBosVolume());
        entity.setBuyLiquidity(data.getBuyLiquidity());
        entity.setBuyLiquidityStrength(data.getBuyLiquidityStrength());
        entity.setSellLiquidity(data.getSellLiquidity());
        entity.setSellLiquidityStrength(data.getSellLiquidityStrength());
        entity.setVolatility(data.getVolatility());
        entity.setAverageVolume(data.getAverageVolume());
        entity.setRsi14(data.getRsi14());
        entity.setTrade(data.getTrade());
        entity.setEntryPrice(data.getEntryPrice());
        entity.setTradeResult(data.getTradeResult());

        return entity;
    }

    public CandleAggregatedData fromEntity(CandleAggregatedDataEntity entity) {
        CandleAggregatedData data = new CandleAggregatedData();

        data.setStockSymbol(entity.getStockSymbol());
        data.setTimeframe(entity.getTimeframe());
        data.setCandleTimestamp(entity.getCandleTimestamp());
        data.setOpen(entity.getOpen());
        data.setClose(entity.getClose());
        data.setHigh(entity.getHigh());
        data.setLow(entity.getLow());
        data.setVolume(entity.getVolume());
        data.setLastSwingHigh(entity.getLastSwingHigh());
        data.setLastSwingLow(entity.getLastSwingLow());
        data.setSupplyPrice(entity.getSupplyPrice());
        data.setSupplyVolume(entity.getSupplyVolume());
        data.setDemandPrice(entity.getDemandPrice());
        data.setDemandVolume(entity.getDemandVolume());
        data.setLastLiquiditySweepType(entity.getLastLiquiditySweepType());
        data.setBosDirection(entity.getBosDirection());
        data.setBosVolume(entity.getBosVolume());
        data.setBuyLiquidity(entity.getBuyLiquidity());
        data.setBuyLiquidityStrength(entity.getBuyLiquidityStrength());
        data.setSellLiquidity(entity.getSellLiquidity());
        data.setSellLiquidityStrength(entity.getSellLiquidityStrength());
        data.setVolatility(entity.getVolatility());
        data.setAverageVolume(entity.getAverageVolume());
        data.setRsi14(entity.getRsi14());
        data.setTrade(entity.getTrade());
        data.setEntryPrice(entity.getEntryPrice());
        data.setTradeResult(entity.getTradeResult());

        return data;
    }
}
