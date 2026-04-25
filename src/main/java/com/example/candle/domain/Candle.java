package com.example.candle.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Candle(
        long time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume
) {
    public Candle {
        Objects.requireNonNull(open, "open cannot be null");
        Objects.requireNonNull(high, "high cannot be null");
        Objects.requireNonNull(low, "low cannot be null");
        Objects.requireNonNull(close, "close cannot be null");

        if (time < 0) {
            throw new IllegalArgumentException("time cannot be negative");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("volume cannot be negative");
        }
        if (high.compareTo(low) < 0) {
            throw new IllegalArgumentException("high cannot be lower than low");
        }
    }

    public static Candle first(long time, BigDecimal price) {
        Objects.requireNonNull(price, "price cannot be null");

        return new Candle(
                time,
                price,
                price,
                price,
                price,
                1
        );
    }

    public Candle update(BigDecimal price) {
        Objects.requireNonNull(price, "price cannot be null");

        return new Candle(
                time,
                open,
                high.compareTo(price) >= 0 ? high : price,
                low.compareTo(price) <= 0 ? low : price,
                price,
                Math.addExact(volume, 1)
        );
    }
}