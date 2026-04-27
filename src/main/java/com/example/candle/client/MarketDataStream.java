package com.example.candle.client;

import com.example.candle.domain.BidAskEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a mocked market data stream for demonstration purposes.
 * In production, it would swap Flux.interval with a WebSocket client.
 */
@Component
public class MarketDataStream {

    private static final List<String> SYMBOLS = List.of("BTC-USD", "ETH-USD", "SOL-USD");

    public Flux<BidAskEvent> streamMarketData() {
        return Flux.interval(Duration.ofMillis(100))
                // Emits a tick for ALL symbols every 100ms
                .flatMapIterable(ignored -> SYMBOLS)
                .map(this::generateRealisticTick)
                .share();
    }

    private BidAskEvent generateRealisticTick(String symbol) {
        double basePrice = switch (symbol) {
            case "BTC-USD" -> 65000.0;
            case "ETH-USD" -> 3500.0;
            case "SOL-USD" -> 150.0;
            default -> 100.0;
        };

        // 1. Create a baseline moving price
        double movement = ThreadLocalRandom.current().nextDouble(-10, 10);
        double midPrice = basePrice + movement;

        // 2. Guarantee a positive spread
        double spread = ThreadLocalRandom.current().nextDouble(0.5, 2.0);

        return new BidAskEvent(
                symbol,
                BigDecimal.valueOf(midPrice - (spread / 2)).setScale(8, RoundingMode.HALF_UP),
                BigDecimal.valueOf(midPrice + (spread / 2)).setScale(8, RoundingMode.HALF_UP),
                Instant.now().toEpochMilli() // Use milliseconds for HFT!
        );
    }
}