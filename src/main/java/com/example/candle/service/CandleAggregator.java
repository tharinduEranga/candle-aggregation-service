package com.example.candle.service;

import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleAggregator {

    private final CandleRepository candleRepository;

    private final List<CandleInterval> supportedIntervals = List.of(
            CandleInterval.ONE_SECOND,
            CandleInterval.FIVE_SECONDS,
            CandleInterval.ONE_MINUTE,
            CandleInterval.FIFTEEN_MINUTES,
            CandleInterval.ONE_HOUR
    );

    public void process(final BidAskEvent event) {
        final BigDecimal price = midPrice(event);

        for (final CandleInterval interval : supportedIntervals) {
            final long bucketTime = interval.bucketStart(event.timestamp());

            final var key = new CandleKey(
                    event.symbol(),
                    interval,
                    bucketTime
            );

            candleRepository.updateCandle(key, price);
        }

        log.debug(
                "Processed market event. symbol={}, bid={}, ask={}, timestamp={}",
                event.symbol(),
                event.bid(),
                event.ask(),
                event.timestamp()
        );
    }

    private BigDecimal midPrice(BidAskEvent event) {
        return event.getPrice();
    }
}