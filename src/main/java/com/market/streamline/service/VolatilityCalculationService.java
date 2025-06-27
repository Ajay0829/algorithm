package com.market.streamline.service;

import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.Volatility;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.VolatilityRepository;
import org.springframework.stereotype.Service;

import static java.lang.Math.abs;

@Service
public class VolatilityCalculationService {

    private final VolatilityRepository volatilityRepository;
    private final CandleRepository candleRepository;

    public VolatilityCalculationService(VolatilityRepository volatilityRepository, CandleRepository candleRepository) {
        this.volatilityRepository = volatilityRepository;
        this.candleRepository = candleRepository;
    }

    public void calculateVolatility(CandleEntity candleEntity) {
        String stockSymbol = candleEntity.getStockSymbol();
        String timeframe = candleEntity.getTimeframe();

        Volatility volatility = volatilityRepository.findByStockSymbolAndTimeframe(
                stockSymbol,
                timeframe
        );

        if (volatility == null) {
            volatility = new Volatility(
                    stockSymbol,
                    timeframe,
                    abs(candleEntity.getOpen() - candleEntity.getClose()) * 100 / candleEntity.getOpen()
            );
        } else {
            double current_volatility = volatility.getVolatility();
            double current_move = abs(candleEntity.getOpen() - candleEntity.getClose()) * 100 / candleEntity.getOpen();
            long totalCandles = candleRepository.countByStockSymbolAndTimeframe(stockSymbol, timeframe);
            double final_volatility = (current_volatility * (totalCandles - 1) + current_move) / totalCandles;
            volatility.setVolatility(final_volatility);

        }
        volatilityRepository.save(volatility);
    }
}
