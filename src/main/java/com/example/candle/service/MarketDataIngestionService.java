package com.example.candle.service;

import com.example.candle.client.MarketDataStream;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

@Service
public class MarketDataIngestionService {

    private final MarketDataStream marketDataStream;
    private final CandleAggregator candleAggregator;

    public MarketDataIngestionService(MarketDataStream marketDataStream, CandleAggregator candleAggregator) {
        this.marketDataStream = marketDataStream;
        this.candleAggregator = candleAggregator;
    }

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