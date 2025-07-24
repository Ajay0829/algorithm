package com.market.streamline.plot;

import com.market.streamline.entity.liquidity.Liquidity;
import com.market.streamline.entity.structure.BreakOfStructure;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.SwingPoint;
import com.market.streamline.entity.trade.Trade;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.plot.charts.*;
import com.market.streamline.plot.kafka.ChartAnnotationProducer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChartAnnotationService {

    private final ChartAnnotationProducer chartAnnotationProducer;

    public ChartAnnotationService(ChartAnnotationProducer chartAnnotationProducer) {
        this.chartAnnotationProducer = chartAnnotationProducer;
    }

    public void processCandle(CandleEntity candleEntity, String type) {
//        ChartDTO chart = new ChartDTO(
//                "candle",
//                type,
//                new CandleData(
//                        candleEntity.getCandleTimestamp().toString(),
//                        candleEntity.getOpen(),
//                        candleEntity.getHigh(),
//                        candleEntity.getLow(),
//                        candleEntity.getClose(),
//                        candleEntity.getVolume(),
//                        candleEntity.getStockSymbol(),
//                        candleEntity.getTimeframe()
//                )
//        );
//
//        chartAnnotationProducer.sendAnnotation(chart);
    }

    public void processSwingPoint(SwingPoint swingPoint, String type) {
//        String high_low = swingPoint.getSwingType().equals("HIGH") ? "high" : "low";
//        String major_minor = swingPoint.getIsMajor() ? "major_" : "minor_";
//        ChartDTO chart = new ChartDTO(
//                "swing",
//                type,
//                new SwingData(
//                        major_minor + high_low,
//                        swingPoint.getCandleTimestamp().toString(),
//                        swingPoint.getSwingType().equals("HIGH"),
//                        swingPoint.getTimeframe()
//                )
//        );
//
//        chartAnnotationProducer.sendAnnotation(chart);
    }

    public void processBreakOfStructure(BreakOfStructure breakOfStructure, String type) {
//        ChartDTO chart = new ChartDTO(
//                "bos",
//                type,
//                new BOSData(
//                        breakOfStructure.getWeakSwingPoint().getCandleTimestamp().toString(),
//                        breakOfStructure.getCandleTimestamp().toString(),
//                        breakOfStructure.getDirection(),
//                        breakOfStructure.getTimeframe(),
//                        breakOfStructure.getWeakSwingPoint().getPrice()
//                )
//        );
//
//        chartAnnotationProducer.sendAnnotation(chart);
    }

    public void processZone(Zone zone, String type) {
//        LocalDateTime nextCandleTimestamp;
//        if (zone.getTimeframe().equals("1d")) {
//            nextCandleTimestamp = zone.getCandleTimestamp().plusDays(15);
//        } else {
//            nextCandleTimestamp = zone.getCandleTimestamp().plusHours(15);
//        }
//        ChartDTO chart = new ChartDTO(
//                "zone",
//                type,
//                new ZoneData(
//                        zone.getZoneType(),
//                        zone.getNearPoint(),
//                        zone.getFarPoint(),
//                        zone.getCandleTimestamp().toString(),
//                        nextCandleTimestamp.toString(),
//                        zone.getTimeframe()
//                )
//        );
//
//        chartAnnotationProducer.sendAnnotation(chart);
    }

    public void processTrade(Trade trade, CandleEntity candleEntity, String type) {
//        ChartDTO chart = new ChartDTO(
//                "trade",
//                type,
//                getTradeData(candleEntity, trade, type)
//        );
//        chartAnnotationProducer.sendAnnotation(chart);
    }

    private TradeData getTradeData(CandleEntity candleEntity, Trade trade, String action) {
        TradeData tradeData = new TradeData(
                trade.getId(),
                trade.getStockSymbol(),
                trade.getTimeframe(),
                trade.getTimestamp().toString(),
                trade.getEntryPrice(),
                trade.getStopLoss(),
                trade.getTakeProfit(),
                trade.getTradeType()
        );

        // Set additional fields needed for immediate plotting
        tradeData.setResult(trade.getResult() != null ? trade.getResult() : "PENDING");
        tradeData.setIsActive(trade.getIsActive());

        // For executed trades, set the entry details immediately
        if ("executed".equals(action)) {
            tradeData.setEntryTimestamp(trade.getTimestamp().toString());
            tradeData.setEntryPrice(trade.getEntryPrice());
        } else if ("updated".equals(action)) {
            if (trade.getResult().equals("LOSS")) {
                tradeData.setResult("LOSS");
                tradeData.setIsActive(false);  // Add this line for consistency
                tradeData.setExitPrice(trade.getStopLoss());
                tradeData.setExitTimestamp(candleEntity.getCandleTimestamp().toString());
            } else {
                tradeData.setResult("WIN");
                tradeData.setIsActive(false);
                tradeData.setExitPrice(trade.getTakeProfit());
                tradeData.setExitTimestamp(candleEntity.getCandleTimestamp().toString());
            }
        }
        return tradeData;
    }

    public void processLiquidity(Liquidity liquidity, String type) {
//        ChartDTO chart = new ChartDTO(
//                "liquidity",
//                type,
//                new LiquidityData(
//                        liquidity.getTimeframe(),
//                        liquidity.getStockSymbol(),
//                        liquidity.getLiquidityType(),
//                        liquidity.getPrice(),
//                        liquidity.getStrength(),
//                        liquidity.getCandleTimestamp().toString()
//                )
//        );
//
//        chartAnnotationProducer.sendAnnotation(chart);
    }
}
