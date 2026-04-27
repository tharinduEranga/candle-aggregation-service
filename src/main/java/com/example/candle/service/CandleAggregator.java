package com.example.candle.service;

import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CandleAggregator {

    private static final Logger log = LoggerFactory.getLogger(CandleAggregator.class);

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

        log.trace("Tick received: symbol={}, bid={}, ask={}, mid={}, timestamp={}",
                event.symbol(), event.bid(), event.ask(), price, event.timestamp());

        for (final CandleInterval interval : supportedIntervals) {
            final long bucketTime = interval.bucketStart(event.timestamp());
            final var key = new CandleKey(event.symbol(), interval, bucketTime);

            activeCandles.compute(key, (k, currentCandle) -> {
                if (currentCandle == null) {
                    log.debug("Opening new candle: symbol={}, interval={}, bucket={}, price={}",
                            k.symbol(), k.interval().getValue(), bucketTime, price);
                    return Candle.first(bucketTime, price);
                }
                return currentCandle.update(price);
            });
        }
    }

    @Scheduled(fixedRate = 1000)
    public void flushToDb() {
        if (activeCandles.isEmpty()) {
            log.trace("Flush tick: no active candles, skipping");
            return;
        }

        final int snapshotSize = activeCandles.size();
        final long currentEpoch = Instant.now().getEpochSecond();
        final AtomicInteger evicted = new AtomicInteger();

        log.debug("Flush starting: activeCandles={}, epoch={}", snapshotSize, currentEpoch);

        activeCandles.forEach((key, candle) -> {

            // 1. ALWAYS flush the latest snapshot to the DB! (Keeps UI real-time)
            candleRepository.updateCandle(key, candle);

            log.trace("Flushed candle: symbol={}, interval={}, bucket={}, close={}, volume={}",
                    key.symbol(), key.interval().getValue(), key.time(), candle.close(), candle.volume());

            // 2. Calculate if this candle's time window is officially over
            long candleEndTime = key.time() + key.interval().getSeconds();

            // 3. EVICTION: If closed, remove it from memory.
            if (currentEpoch >= candleEndTime) {
                // Because Candle is immutable, remove(key, value) is perfectly thread-safe.
                // It only removes if a new tick hasn't updated it in the last millisecond
                if (activeCandles.remove(key, candle)) {
                    evicted.incrementAndGet();
                    log.debug("Evicted closed candle: symbol={}, interval={}, bucket={}",
                            key.symbol(), key.interval().getValue(), key.time());
                }
            }
        });

        log.info("Flush complete: flushed={}, evicted={}, remaining={}",
                snapshotSize, evicted.get(), activeCandles.size());
    }
}
