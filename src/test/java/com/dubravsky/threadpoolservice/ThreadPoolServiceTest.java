package com.dubravsky.threadpoolservice;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ThreadPoolServiceTest {

    static final long SHORT_DELAY = 30L;
    static final long DELAY = 3 * SHORT_DELAY;
    static final String ANY_THREAD_POOL_NAME = "TestThreadPool";
    static final String SECOND_ANY_THREAD_POOL_NAME = "SecondTestThreadPool";
    static final IllegalArgumentException ANY_EXCEPTION = new IllegalArgumentException("Message");
    private static final long STATISTICS_DELAY = SHORT_DELAY;

    private ThreadPoolService threadPoolService;

    @After
    public void shutdown() {
        if (threadPoolService != null) {
            threadPoolService.shutdown();
        }
    }

    @Test
    public void shouldCreateService() {
        threadPoolService = ThreadPoolService.create();
        assertThat(threadPoolService, is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfStatisticsOutputDelayIsNegative() {
        threadPoolService = ThreadPoolService.builder()
                .statisticsOutputDelay(-1L)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfStatisticsOutputDelayIsZero() {
        threadPoolService = ThreadPoolService.builder()
                .statisticsOutputDelay(0L)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfConsumerIsNull() {
        threadPoolService = ThreadPoolService.builder()
                .statisticsOutputDelay(STATISTICS_DELAY)
                .statisticsHandler(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfThreadPoolNameIsNotUnique() {
        threadPoolService = ThreadPoolService.create();

        threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
        threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
    }

    @Test
    public void shouldPrintStats() {
        StatisticsHandler statisticsConsumer = mock(StatisticsHandler.class);

        threadPoolService = ThreadPoolService.builder()
                .statisticsHandler(statisticsConsumer)
                .statisticsOutputDelay(STATISTICS_DELAY)
                .build();

        verify(statisticsConsumer, timeout(3 * STATISTICS_DELAY).atLeastOnce()).handle(any(), anyString());
    }

    @Test
    public void shouldShutdonwnNow() {
        threadPoolService = ThreadPoolService.create();
        ScheduledExecutorService firstThreadPool = createFirstScheduledThreadPool();
        ScheduledExecutorService secondThreadPool = createSecondScheduledThreadPool();

        assertThat(firstThreadPool.isShutdown(), is(false));
        assertThat(secondThreadPool.isShutdown(), is(false));

        threadPoolService.shutdownNow();

        assertThat(firstThreadPool.isShutdown(), is(true));
        assertThat(secondThreadPool.isShutdown(), is(true));
    }

    @Test
    public void shouldCheckIsShutdownCorrectly() {
        threadPoolService = ThreadPoolService.create();
        ScheduledExecutorService firstThreadPool = createFirstScheduledThreadPool();
        ScheduledExecutorService secondThreadPool = createSecondScheduledThreadPool();

        assertThat(firstThreadPool.isShutdown(), is(false));
        assertThat(secondThreadPool.isShutdown(), is(false));
        assertThat(threadPoolService.isShutdown(), is(false));

        threadPoolService.shutdownNow();

        assertThat(firstThreadPool.isShutdown(), is(true));
        assertThat(secondThreadPool.isShutdown(), is(true));
        assertThat(threadPoolService.isShutdown(), is(true));
    }

    @Test
    public void shouldCheckIsTerminatedCorrectly() {
        threadPoolService = ThreadPoolService.create();
        ScheduledExecutorService firstThreadPool = createFirstScheduledThreadPool();
        ScheduledExecutorService secondThreadPool = createSecondScheduledThreadPool();

        assertThat(firstThreadPool.isTerminated(), is(false));
        assertThat(secondThreadPool.isTerminated(), is(false));
        assertThat(threadPoolService.isTerminated(), is(false));

        threadPoolService.shutdownNow();

        assertThat(firstThreadPool.isTerminated(), is(true));
        assertThat(secondThreadPool.isTerminated(), is(true));
        assertThat(threadPoolService.isTerminated(), is(true));
    }

    @Test
    public void shouldCreateSingleExecutorService() {
        threadPoolService = ThreadPoolService.create();
        Runnable task = mock(Runnable.class);

        ExecutorService executorService = threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
        executorService.submit(task);

        verify(task, timeout(DELAY).times(1)).run();
    }

    @Test
    public void shouldCreateFixedExecutorService() {
        threadPoolService = ThreadPoolService.create();
        Runnable task = mock(Runnable.class);

        ExecutorService executorService = threadPoolService.newFixedThreadPool(2, ANY_THREAD_POOL_NAME);
        executorService.submit(task);

        verify(task, timeout(DELAY).times(1)).run();
    }

    @Test
    public void shouldCreateSingleScheduledExecutorService() {
        threadPoolService = ThreadPoolService.create();
        Runnable task = mock(Runnable.class);

        ScheduledExecutorService scheduledExecutorService = createFirstScheduledThreadPool();
        scheduledExecutorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(task, timeout(DELAY).times(1)).run();
    }

    @Test
    public void shouldCreateFixedScheduledExecutorService() {
        threadPoolService = ThreadPoolService.create();
        Runnable task = mock(Runnable.class);

        ScheduledExecutorService scheduledExecutorService = threadPoolService.newScheduledThreadPool(2, ANY_THREAD_POOL_NAME);
        scheduledExecutorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(task, timeout(DELAY).times(1)).run();
    }

    private ScheduledExecutorService createFirstScheduledThreadPool() {
        return threadPoolService.newSingleScheduledThreadPool(ANY_THREAD_POOL_NAME);
    }

    private ScheduledExecutorService createSecondScheduledThreadPool() {
        return threadPoolService.newSingleScheduledThreadPool(SECOND_ANY_THREAD_POOL_NAME);
    }

}
