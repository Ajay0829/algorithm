package com.market.streamline.service;

import com.market.streamline.entity.CandleAggregatedDataEntity;
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
        entity.setDemandPrice(data.getDemandPrice());
        entity.setLastLiquiditySweepType(data.isLastLiquiditySweepType());
        entity.setBosDirection(data.isBosDirection());
        entity.setBuyLiquidity(data.getBuyLiquidity());
        entity.setSellLiquidity(data.getSellLiquidity());
        entity.setVolatility(data.getVolatility());
        entity.setTrade(data.getTrade());
        entity.setEntryPrice(data.getEntryPrice());
        entity.setTargetPrice(data.getTargetPrice());
        entity.setStopLossPrice(data.getStopLossPrice());
        entity.setTradeResult(data.isTradeResult());

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
        data.setDemandPrice(entity.getDemandPrice());
        data.setLastLiquiditySweepType(entity.getLastLiquiditySweepType());
        data.setBosDirection(entity.getBosDirection());
        data.setBuyLiquidity(entity.getBuyLiquidity());
        data.setSellLiquidity(entity.getSellLiquidity());
        data.setVolatility(entity.getVolatility());
        data.setTrade(entity.getTrade());
        data.setEntryPrice(entity.getEntryPrice());
        data.setTargetPrice(entity.getTargetPrice());
        data.setStopLossPrice(entity.getStopLossPrice());
        data.setTradeResult(entity.isTradeResult());

        return data;
    }
}
