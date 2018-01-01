package com.dubravsky.threadpoolservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.dubravsky.threadpoolservice.ThreadPoolServiceTest.*;
import static org.mockito.Mockito.*;

public class ExceptionHandlerTest {

    private ThreadPoolService threadPoolService;
    private Consumer<Exception> exceptionHandler;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    @Before
    public void init() {
        exceptionHandler = mock(Consumer.class);
        threadPoolService = ThreadPoolService.builder()
                .exceptionHandler(exceptionHandler)
                .build();
        executorService = threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
        scheduledExecutorService = threadPoolService.newSingleScheduledThreadPool(SECOND_ANY_THREAD_POOL_NAME);
    }

    @After
    public void shutdown() {
        threadPoolService.shutdown();
    }

    @Test
    public void executorServiceShouldCatchExceptionInCallable() {
        Callable<String> task = () -> {
            throw ANY_EXCEPTION;
        };
        executorService.submit(task);

        verify(exceptionHandler, timeout(DELAY).times(1)).accept(ANY_EXCEPTION);
    }

    @Test
    public void executorServiceShouldCatchExceptionInRunnable() {
        Runnable task = () -> {
            throw ANY_EXCEPTION;
        };
        executorService.submit(task);

        verify(exceptionHandler, timeout(DELAY).times(1)).accept(ANY_EXCEPTION);
    }

    @Test
    public void scheduledExecutorServiceShouldCatchExceptionInCallable() {
        Callable<String> task = () -> {
            throw ANY_EXCEPTION;
        };
        scheduledExecutorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(exceptionHandler, timeout(DELAY).times(1)).accept(ANY_EXCEPTION);
    }

    @Test
    public void scheduledExecutorServiceShouldCatchExceptionInRunnable() {
        Runnable task = () -> {
            throw ANY_EXCEPTION;
        };
        scheduledExecutorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(exceptionHandler, timeout(DELAY).times(1)).accept(ANY_EXCEPTION);
    }

}
