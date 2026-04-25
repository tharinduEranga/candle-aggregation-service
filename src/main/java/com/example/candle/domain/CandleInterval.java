package com.example.candle.domain;

import java.util.Arrays;

public enum CandleInterval {

    ONE_SECOND("1s", 1),
    FIVE_SECONDS("5s", 5),
    ONE_MINUTE("1m", 60),
    FIFTEEN_MINUTES("15m", 900),
    ONE_HOUR("1h", 3600);

    private final String value;
    private final long seconds;

    CandleInterval(String value, long seconds) {
        this.value = value;
        this.seconds = seconds;
    }

    public String getValue() {
        return value;
    }

    public long getSeconds() {
        return seconds;
    }

    public long bucketStart(long timestamp) {
        return timestamp - (timestamp % seconds);
    }

    public static CandleInterval from(String value) {
        return Arrays.stream(values())
                .filter(interval -> interval.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported interval: " + value));
    }
}