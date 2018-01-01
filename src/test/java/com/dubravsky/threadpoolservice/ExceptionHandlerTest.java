package com.dubravsky.threadpoolservice;

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

    @Test
    public void executorServiceShouldCatchExceptionInCallable() {
        Consumer<Exception> exceptionHandler = mock(Consumer.class);
        threadPoolService = new ThreadPoolService();
        threadPoolService.setExceptionHandler(exceptionHandler);

        ExecutorService executorService = threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
        Callable<String> task = () -> {
            throw ANY_EXCEPTION;
        };
        executorService.submit(task);

        verify(exceptionHandler, timeout(3 * SHORT_DELAY).times(1)).accept(ANY_EXCEPTION);
    }

    @Test
    public void executorServiceShouldCatchExceptionInRunnable() {
        Consumer<Exception> exceptionHandler = mock(Consumer.class);
        threadPoolService = new ThreadPoolService();
        threadPoolService.setExceptionHandler(exceptionHandler);

        ExecutorService executorService = threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
        Runnable task = () -> {
            throw ANY_EXCEPTION;
        };
        executorService.submit(task);

        verify(exceptionHandler, timeout(3 * SHORT_DELAY).times(1)).accept(ANY_EXCEPTION);
    }

    @Test
    public void scheduledExecutorServiceShouldCatchExceptionInCallable() {
        Consumer<Exception> exceptionHandler = mock(Consumer.class);
        threadPoolService = new ThreadPoolService();
        threadPoolService.setExceptionHandler(exceptionHandler);

        ScheduledExecutorService executorService = threadPoolService.newSingleScheduledThreadPool(ANY_THREAD_POOL_NAME);
        Callable<String> task = () -> {
            throw ANY_EXCEPTION;
        };
        executorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(exceptionHandler, timeout(3 * SHORT_DELAY).times(1)).accept(ANY_EXCEPTION);
    }

    @Test
    public void scheduledExecutorServiceShouldCatchExceptionInRunnable() {
        Consumer<Exception> exceptionHandler = mock(Consumer.class);
        threadPoolService = new ThreadPoolService();
        threadPoolService.setExceptionHandler(exceptionHandler);

        ScheduledExecutorService executorService = threadPoolService.newSingleScheduledThreadPool(ANY_THREAD_POOL_NAME);
        Runnable task = () -> {
            throw ANY_EXCEPTION;
        };
        executorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(exceptionHandler, timeout(3 * SHORT_DELAY).times(1)).accept(ANY_EXCEPTION);
    }

}
