package com.example.candle.repository;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;

import java.math.BigDecimal;
import java.util.List;

public interface CandleRepository {

    void updateCandle(CandleKey key, BigDecimal price);

    List<Candle> findCandles(
            String symbol,
            CandleInterval interval,
            long from,
            long to
    );

    long count();
}