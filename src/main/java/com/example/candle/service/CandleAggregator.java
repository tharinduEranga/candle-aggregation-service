package com.example.candle.service;

import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CandleAggregator {

    private final CandleRepository candleRepository;

    private final Map<CandleKey, Candle> activeCandles = new ConcurrentHashMap<>();

    private final List<CandleInterval> supportedIntervals = List.of(
            CandleInterval.ONE_SECOND,
            CandleInterval.FIVE_SECONDS,
            CandleInterval.ONE_MINUTE,
            CandleInterval.FIFTEEN_MINUTES,
            CandleInterval.ONE_HOUR
    );

    public CandleAggregator(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public void process(final BidAskEvent event) {
        final BigDecimal price = event.getPrice();

        for (final CandleInterval interval : supportedIntervals) {
            final long bucketTime = interval.bucketStart(event.timestamp());
            final var key = new CandleKey(event.symbol(), interval, bucketTime);

            activeCandles.compute(key, (k, currentCandle) -> {
                if (currentCandle == null) {
                    return Candle.first(bucketTime, price);
                }
                return currentCandle.update(price);
            });
        }
    }

    @Scheduled(fixedRate = 1000)
    public void flushToDb() {
        if (activeCandles.isEmpty()) return;

        long currentEpoch = Instant.now().getEpochSecond();

        activeCandles.forEach((key, candle) -> {

            // 1. ALWAYS flush the latest snapshot to the DB! (Keeps UI real-time)
            candleRepository.updateCandle(key, candle);

            // 2. Calculate if this candle's time window is officially over
            long candleEndTime = key.time() + key.interval().getSeconds();

            // 3. EVICTION: If closed, remove it from memory.
            if (currentEpoch >= candleEndTime) {
                // Because Candle is immutable, remove(key, value) is perfectly thread-safe.
                // It only removes if a new tick hasn't updated it in the last millisecond
                activeCandles.remove(key, candle);
            }
        });
    }
}