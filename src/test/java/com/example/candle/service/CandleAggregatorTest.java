package com.example.candle.service;

import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CandleAggregatorTest {

    @Mock
    private CandleRepository candleRepository;

    @Test
    void process_buffersInMemoryWithoutTouchingRepository() {
        CandleAggregator candleAggregator = new CandleAggregator(candleRepository);

        BidAskEvent event = new BidAskEvent(
                "BTC-USD",
                new BigDecimal("50000.00"),
                new BigDecimal("50002.00"),
                3_671L
        );

        candleAggregator.process(event);

        verifyNoInteractions(candleRepository);
    }

    @Test
    void flushToDb_writesAllSupportedIntervalsWithExpectedKeysAndMidPrice() {
        CandleAggregator candleAggregator = new CandleAggregator(candleRepository);

        BidAskEvent event = new BidAskEvent(
                "BTC-USD",
                new BigDecimal("50000.00"),
                new BigDecimal("50002.00"),
                3_671L
        );

        candleAggregator.process(event);
        candleAggregator.flushToDb();

        ArgumentCaptor<CandleKey> keyCaptor = ArgumentCaptor.forClass(CandleKey.class);
        ArgumentCaptor<Candle> candleCaptor = ArgumentCaptor.forClass(Candle.class);

        verify(candleRepository, times(5)).updateCandle(keyCaptor.capture(), candleCaptor.capture());
        verifyNoMoreInteractions(candleRepository);

        Set<CandleKey> expectedKeys = Set.of(
                new CandleKey("BTC-USD", CandleInterval.ONE_SECOND, 3_671L),
                new CandleKey("BTC-USD", CandleInterval.FIVE_SECONDS, 3_670L),
                new CandleKey("BTC-USD", CandleInterval.ONE_MINUTE, 3_660L),
                new CandleKey("BTC-USD", CandleInterval.FIFTEEN_MINUTES, 3_600L),
                new CandleKey("BTC-USD", CandleInterval.ONE_HOUR, 3_600L)
        );

        assertEquals(expectedKeys, Set.copyOf(keyCaptor.getAllValues()));

        BigDecimal expectedPrice = new BigDecimal("50001.00000000");
        for (Candle captured : candleCaptor.getAllValues()) {
            assertEquals(expectedPrice, captured.open());
            assertEquals(expectedPrice, captured.high());
            assertEquals(expectedPrice, captured.low());
            assertEquals(expectedPrice, captured.close());
            assertEquals(1L, captured.volume());
        }
    }

    @Test
    void flushToDb_usesRoundedMidPriceFromBidAndAsk() {
        CandleAggregator candleAggregator = new CandleAggregator(candleRepository);

        BidAskEvent event = new BidAskEvent(
                "ETH-USD",
                new BigDecimal("1.00000001"),
                new BigDecimal("1.00000002"),
                10L
        );

        candleAggregator.process(event);
        candleAggregator.flushToDb();

        ArgumentCaptor<Candle> candleCaptor = ArgumentCaptor.forClass(Candle.class);
        verify(candleRepository, times(5)).updateCandle(any(CandleKey.class), candleCaptor.capture());

        BigDecimal expectedPrice = new BigDecimal("1.00000002");
        for (Candle captured : candleCaptor.getAllValues()) {
            assertEquals(expectedPrice, captured.close());
        }
    }

    @Test
    void flushToDb_doesNothingWhenNoEventsBuffered() {
        CandleAggregator candleAggregator = new CandleAggregator(candleRepository);

        candleAggregator.flushToDb();

        verifyNoInteractions(candleRepository);
    }
}
