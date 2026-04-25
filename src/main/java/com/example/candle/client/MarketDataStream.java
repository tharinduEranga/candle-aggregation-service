package com.example.candle.client;

import com.example.candle.domain.BidAskEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulated stream.
 * In production, you would swap Flux.interval with a WebSocket client.
 */
@Component
public class MarketDataStream {

    // Simulates receiving a new tick every 100ms
    public Flux<BidAskEvent> streamMarketData() {
        return Flux.interval(Duration.ofMillis(100))
                .map(tick -> new BidAskEvent(
                        "BTC-USD",
                        BigDecimal.valueOf(50000 + ThreadLocalRandom.current().nextDouble(-10, 10)),
                        BigDecimal.valueOf(50001 + ThreadLocalRandom.current().nextDouble(-10, 10)),
                        Instant.now().getEpochSecond()
                ))
                .share(); // Crucial: Shares one stream with multiple subscribers!
    }
}