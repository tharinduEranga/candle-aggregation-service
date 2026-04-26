package com.example.candle.service;

import com.example.candle.domain.Candle;
import com.example.candle.domain.CandleInterval;
import com.example.candle.repository.CandleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final CandleRepository candleRepository;

    public HistoryService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public List<Candle> getHistory(String symbol, CandleInterval interval, long from, long to) {
        return candleRepository.findCandles(symbol, interval, from, to);
    }
}