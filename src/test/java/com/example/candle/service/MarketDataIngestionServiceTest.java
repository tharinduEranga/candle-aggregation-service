package com.example.candle.service;

import com.example.candle.client.MarketDataStream;
import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
class MarketDataIngestionServiceTest {

    @Test
    void start_subscribesToStreamAndProcessesIncomingEvents() {
        BidAskEvent first = new BidAskEvent("BTC-USD", new BigDecimal("100.00"), new BigDecimal("101.00"), 1_000L);
        BidAskEvent second = new BidAskEvent("BTC-USD", new BigDecimal("101.00"), new BigDecimal("102.00"), 1_001L);

        MarketDataStream marketDataStream = new MarketDataStream() {
            @Override
            public Flux<BidAskEvent> streamMarketData() {
                return Flux.just(first, second);
            }
        };

        RecordingCandleAggregator candleAggregator = new RecordingCandleAggregator();
        MarketDataIngestionService service = new MarketDataIngestionService(marketDataStream, candleAggregator);

        service.start();

        assertEquals(List.of(first, second), candleAggregator.processedEvents);
    }

    @Test
    void stop_disposesActiveSubscription() {
        AtomicBoolean cancelled = new AtomicBoolean(false);

        MarketDataStream marketDataStream = new MarketDataStream() {
            @Override
            public Flux<BidAskEvent> streamMarketData() {
                return Flux.<BidAskEvent>never().doOnCancel(() -> cancelled.set(true));
            }
        };

        RecordingCandleAggregator candleAggregator = new RecordingCandleAggregator();
        MarketDataIngestionService service = new MarketDataIngestionService(marketDataStream, candleAggregator);

        service.start();
        service.stop();

        assertTrue(cancelled.get());
    }

    private static final class RecordingCandleAggregator extends CandleAggregator {
        private final List<BidAskEvent> processedEvents = new ArrayList<>();

        private RecordingCandleAggregator() {
            super(new NoOpCandleRepository());
        }

        @Override
        public void process(BidAskEvent event) {
            processedEvents.add(event);
        }
    }

    private static final class NoOpCandleRepository implements CandleRepository {

        @Override
        public void updateCandle(CandleKey key, Candle candle) {
        }

        @Override
        public List<Candle> findCandles(String symbol, CandleInterval interval, long from, long to) {
            return List.of();
        }
    }
}
