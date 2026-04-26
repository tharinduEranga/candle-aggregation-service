package com.example.candle.service;

import com.example.candle.domain.BidAskEvent;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import com.example.candle.repository.CandleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CandleAggregatorTest {

    @Mock
    private CandleRepository candleRepository;

    @Test
    void process_updatesAllSupportedIntervalsWithExpectedKeysAndMidPrice() {
        CandleAggregator candleAggregator = new CandleAggregator(candleRepository);

        BidAskEvent event = new BidAskEvent(
                "BTC-USD",
                new BigDecimal("50000.00"),
                new BigDecimal("50002.00"),
                3_671L
        );

        candleAggregator.process(event);

        ArgumentCaptor<CandleKey> keyCaptor = ArgumentCaptor.forClass(CandleKey.class);
        ArgumentCaptor<BigDecimal> priceCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(candleRepository, times(5)).updateCandle(keyCaptor.capture(), priceCaptor.capture());
        verifyNoMoreInteractions(candleRepository);

        List<CandleKey> expectedKeys = List.of(
                new CandleKey("BTC-USD", CandleInterval.ONE_SECOND, 3_671L),
                new CandleKey("BTC-USD", CandleInterval.FIVE_SECONDS, 3_670L),
                new CandleKey("BTC-USD", CandleInterval.ONE_MINUTE, 3_660L),
                new CandleKey("BTC-USD", CandleInterval.FIFTEEN_MINUTES, 3_600L),
                new CandleKey("BTC-USD", CandleInterval.ONE_HOUR, 3_600L)
        );

        assertEquals(expectedKeys, keyCaptor.getAllValues());

        BigDecimal expectedPrice = new BigDecimal("50001.00000000");
        for (BigDecimal capturedPrice : priceCaptor.getAllValues()) {
            assertEquals(expectedPrice, capturedPrice);
        }
    }

    @Test
    void process_usesRoundedMidPriceFromBidAndAsk() {
        CandleAggregator candleAggregator = new CandleAggregator(candleRepository);

        BidAskEvent event = new BidAskEvent(
                "ETH-USD",
                new BigDecimal("1.00000001"),
                new BigDecimal("1.00000002"),
                10L
        );

        candleAggregator.process(event);

        ArgumentCaptor<BigDecimal> priceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(candleRepository, times(5)).updateCandle(org.mockito.ArgumentMatchers.any(CandleKey.class), priceCaptor.capture());

        BigDecimal expectedPrice = new BigDecimal("1.00000002");
        for (BigDecimal capturedPrice : priceCaptor.getAllValues()) {
            assertEquals(expectedPrice, capturedPrice);
        }
    }
}
