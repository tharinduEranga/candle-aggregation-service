package com.example.candle.repository;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class CandleStore implements CandleRepository {

    private final Map<String, Map<CandleInterval, ConcurrentSkipListMap<Long, Candle>>> store =
            new ConcurrentHashMap<>();

    @Override
    public void updateCandle(CandleKey key, BigDecimal price) {
        var intervalMap = store.computeIfAbsent(
                key.symbol(),
                ignored -> new ConcurrentHashMap<>()
        );

        var timeMap = intervalMap.computeIfAbsent(
                key.interval(),
                ignored -> new ConcurrentSkipListMap<>()
        );

        timeMap.compute(
                key.time(),
                (time, existingCandle) -> existingCandle == null
                        ? Candle.first(time, price)
                        : existingCandle.update(price)
        );
    }

    @Override
    public List<Candle> findCandles(
            String symbol,
            CandleInterval interval,
            long from,
            long to
    ) {
        var intervalMap = store.get(symbol);

        if (intervalMap == null) {
            return List.of();
        }

        var timeMap = intervalMap.get(interval);

        if (timeMap == null) {
            return List.of();
        }

        return timeMap.subMap(from, true, to, true)
                .values()
                .stream()
                .toList();
    }

    @Override
    public long count() {
        return store.values()
                .stream()
                .flatMap(intervalMap -> intervalMap.values().stream())
                .mapToLong(Map::size)
                .sum();
    }
}