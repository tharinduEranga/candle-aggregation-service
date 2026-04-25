package com.example.candle.service;

import com.example.candle.client.MarketDataStream;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

@Service
@RequiredArgsConstructor
public class MarketDataIngestionService {

    private final MarketDataStream marketDataStream;
    private final CandleAggregator candleAggregator;

    private Disposable subscription;

    @PostConstruct
    public void start() {
        subscription = marketDataStream.streamMarketData()
                .doOnNext(candleAggregator::process)
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        subscription.dispose();
    }
}