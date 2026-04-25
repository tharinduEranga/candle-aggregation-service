package com.example.candle.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record BidAskEvent(
        String symbol,
        BigDecimal bid,
        BigDecimal ask,
        long timestamp
) {
    public BidAskEvent {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or blank");
        }
        Objects.requireNonNull(bid, "bid cannot be null");
        Objects.requireNonNull(ask, "ask cannot be null");
        if (timestamp < 0) {
            throw new IllegalArgumentException("Timestamp cannot be negative");
        }
    }

    // Helper to get the mid-price for the candle calculation
    public BigDecimal getPrice() {
        return bid
                .add(ask)
                .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
    }
}