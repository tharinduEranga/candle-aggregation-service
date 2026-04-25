package com.example.candle.service;

import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CandleAggregator {

    private static final Logger log = LoggerFactory.getLogger(CandleAggregator.class);

    private final CandleRepository candleRepository;

    public CandleAggregator(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

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

        log.info(
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