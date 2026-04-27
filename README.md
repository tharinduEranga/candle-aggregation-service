# Candle Aggregation Service

A high-frequency market data aggregator that turns a continuous tick stream into time-aligned OHLCV candles.

## Project Overview

- **Core goal:** High-frequency market data aggregator service.
- **Input:** Continuous simulated stream of `BidAskEvent` ticks.
- **Output:** Time-aligned `Candle` objects (OHLCV).
- **Supported intervals:** `1s`, `5s`, `1m`, `15m`, `1h`.
- **Tech stack:** Java 21, Spring Boot WebFlux, PostgreSQL.
- **Architecture:** Hybrid reactive streaming + virtual threads.

## Assumptions & Trade-offs

### Assumption 1 — Frontend UIs require "live" partial candle states

- **Trade-off (storage):** Chose JDBC on virtual threads over R2DBC.
  - *Reasoning:* keeps the DB layer maintainable while still avoiding thread-blocking under load.
- **Trade-off (aggregation):** In-memory `ConcurrentHashMap` instead of writing to the DB on every tick.
  - *Reasoning:* per-tick writes would overwhelm the database.
  - *Solution:* in-memory snapshots are flushed asynchronously every 1 second.

### Assumption 2 — Immutable Java records are sufficient for thread safety

Domain objects (`Candle`, `CandleKey`, `BidAskEvent`) are `record`s, so they're inherently safe to share across threads.

## Running the Service

### Prerequisites

- Java 21
- Maven
- A running PostgreSQL instance (defaults: `localhost:5432`, db `candles`, user/password `postgres`/`postgres`)

Datasource values can be overridden via environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### Commands

```bash
# Run the unit tests
mvn clean test

# Start the application
mvn spring-boot:run
```

### Troubleshooting

If you're running on Java 25, add `-Dnet.bytebuddy.experimental=true` to your JVM args to keep Mockito happy.

## Bonus Features

- **Smart memory eviction** — custom logic removes fully-closed candles from the in-memory map to prevent leaks.
- **Volume accuracy** — the SQL `UPSERT` uses `volume = EXCLUDED.volume` to avoid double-counting on re-flush.
- **Concurrency safety on flush** — `Map.remove(key, value)` is used to prevent races between the ingest path and the flush path.
- **Financial precision** — all price math goes through `BigDecimal` to eliminate floating-point error.
