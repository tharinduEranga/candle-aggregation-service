package com.example.candle.repository;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;

import java.util.List;

public interface CandleRepository {

    void updateCandle(CandleKey key, Candle candle);

    List<Candle> findCandles(
            String symbol,
            CandleInterval interval,
            long from,
            long to
    );
}