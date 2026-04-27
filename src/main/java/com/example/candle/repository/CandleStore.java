package com.example.candle.repository;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.domain.CandleKey;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CandleStore implements CandleRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO candles (symbol, interval_code, bucket_time, open, high, low, close, volume)
            VALUES (:symbol, :interval, :time, :open, :high, :low, :close, :volume)
            ON CONFLICT (symbol, interval_code, bucket_time) DO UPDATE SET
                high   = GREATEST(candles.high, EXCLUDED.high),
                low    = LEAST(candles.low, EXCLUDED.low),
                close  = EXCLUDED.close,
                volume = EXCLUDED.volume 
            """;

    private static final String SELECT_RANGE_SQL = """
            SELECT bucket_time, open, high, low, close, volume
            FROM candles
            WHERE symbol = :symbol
              AND interval_code = :interval
              AND bucket_time BETWEEN :from AND :to
            ORDER BY bucket_time
            """;

    private final JdbcClient jdbcClient;

    public CandleStore(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void updateCandle(CandleKey key, Candle candle) {
        jdbcClient.sql(UPSERT_SQL)
                .param("symbol", key.symbol())
                .param("interval", key.interval().getValue())
                .param("time", key.time())
                .param("open", candle.open())
                .param("high", candle.high())
                .param("low", candle.low())
                .param("close", candle.close())
                .param("volume", candle.volume())
                .update();
    }

    @Override
    public List<Candle> findCandles(String symbol, CandleInterval interval, long from, long to) {
        return jdbcClient.sql(SELECT_RANGE_SQL)
                .param("symbol", symbol)
                .param("interval", interval.getValue())
                .param("from", from)
                .param("to", to)
                .query(Candle.class)
                .list();
    }
}