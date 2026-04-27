package com.example.candle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.AsyncTaskExecutor;
import com.example.candle.repository.CandleRepository;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class CandleAggregationServiceApplicationTests {

    @MockBean
    private CandleRepository candleRepository;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor applicationTaskExecutor;

    @Test
    void contextLoads() {
    }

    @Test
    void usesVirtualThreadsForAsyncTasks() throws Exception {
        final CompletableFuture<Thread> executedThread = new CompletableFuture<>();
        final Method isVirtual = Thread.class.getMethod("isVirtual");

        applicationTaskExecutor.execute(() -> executedThread.complete(Thread.currentThread()));

        assertTrue((Boolean) isVirtual.invoke(executedThread.get(5, TimeUnit.SECONDS)));
    }
}