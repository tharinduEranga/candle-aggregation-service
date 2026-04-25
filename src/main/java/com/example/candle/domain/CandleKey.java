package com.example.candle.domain;

public record CandleKey(
        String symbol,
        CandleInterval interval,
        long time
) {
}
