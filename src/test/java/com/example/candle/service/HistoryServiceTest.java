package com.example.candle.service;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.repository.CandleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private CandleRepository candleRepository;

    @Test
    void getHistory_delegatesToRepositoryAndReturnsResult() {
        HistoryService historyService = new HistoryService(candleRepository);

        Candle candle = new Candle(
                1_620_000_000L,
                new BigDecimal("50000.00000000"),
                new BigDecimal("50010.00000000"),
                new BigDecimal("49990.00000000"),
                new BigDecimal("50005.00000000"),
                12L
        );

        when(candleRepository.findCandles("BTC-USD", CandleInterval.ONE_MINUTE, 1_620_000_000L, 1_620_000_600L))
                .thenReturn(List.of(candle));

        List<Candle> actual = historyService.getHistory("BTC-USD", CandleInterval.ONE_MINUTE, 1_620_000_000L, 1_620_000_600L);

        assertEquals(List.of(candle), actual);
        verify(candleRepository).findCandles("BTC-USD", CandleInterval.ONE_MINUTE, 1_620_000_000L, 1_620_000_600L);
    }
}