package com.example.candle.controller;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.service.HistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private HistoryService historyService;

    @Test
    void getHistory_returnsCandlesForValidRequest() {
        Candle candle = new Candle(
                1_620_000_000L,
                new BigDecimal("50000.00000000"),
                new BigDecimal("50010.00000000"),
                new BigDecimal("49990.00000000"),
                new BigDecimal("50005.00000000"),
                12L
        );

        when(historyService.getHistory("BTC-USD", CandleInterval.ONE_MINUTE, 1_620_000_000L, 1_620_000_600L))
                .thenReturn(List.of(candle));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/history")
                        .queryParam("symbol", "BTC-USD")
                        .queryParam("interval", "1m")
                        .queryParam("from", 1_620_000_000L)
                        .queryParam("to", 1_620_000_600L)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Candle.class)
                .value(candles -> {
                    assertEquals(1, candles.size());
                    assertEquals(candle, candles.getFirst());
                });

        verify(historyService).getHistory("BTC-USD", CandleInterval.ONE_MINUTE, 1_620_000_000L, 1_620_000_600L);
    }

    @Test
    void getHistory_returnsBadRequestForUnsupportedInterval() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/history")
                        .queryParam("symbol", "BTC-USD")
                        .queryParam("interval", "10m")
                        .queryParam("from", 1_620_000_000L)
                        .queryParam("to", 1_620_000_600L)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(historyService);
    }

    @Test
    void getHistory_returnsBadRequestWhenFromIsGreaterThanTo() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/history")
                        .queryParam("symbol", "BTC-USD")
                        .queryParam("interval", "1m")
                        .queryParam("from", 1_620_000_600L)
                        .queryParam("to", 1_620_000_000L)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(historyService);
    }
}