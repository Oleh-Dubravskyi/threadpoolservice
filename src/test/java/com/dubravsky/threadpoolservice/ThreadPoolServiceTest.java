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
    static final String ANY_THREAD_POOL_NAME = "TestThreadPool";
    static final IllegalArgumentException ANY_EXCEPTION = new IllegalArgumentException("Message");
    private static final long STATISTICS_DELAY = SHORT_DELAY;
    private static final String SECOND_ANY_THREAD_POOL_NAME = "SecondTestThreadPool";

    private ThreadPoolService threadPoolService;

    @After
    public void shutdown() {
        if (threadPoolService != null) {
            threadPoolService.shutdown();
        }
    }

    @Test
    public void shouldCreateService() {
        threadPoolService = new ThreadPoolService();
        assertThat(threadPoolService, is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetupStatisticsPrintingIfDelayIsNegative() {
        threadPoolService = new ThreadPoolService();
        threadPoolService.setupStatisticsPrinting(-1L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetupStatisticsPrintingIfDelayIsZero() {
        threadPoolService = new ThreadPoolService();
        threadPoolService.setupStatisticsPrinting(0L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSetupStatisticsPrintingIfConsumerIsNull() {
        threadPoolService = new ThreadPoolService();
        threadPoolService.setupStatisticsPrinting(STATISTICS_DELAY, null);
    }

    @Test
    public void shouldPrintStats() {
        Consumer<String> statisticsConsumer = mock(Consumer.class);

        threadPoolService = new ThreadPoolService();
        threadPoolService.setupStatisticsPrinting(STATISTICS_DELAY, statisticsConsumer);

        verify(statisticsConsumer, timeout(3 * STATISTICS_DELAY).atLeastOnce()).accept(anyString());
    }

    @Test
    public void shouldReassingPrintingConsumer() {
        Consumer<String> firstStatisticsConsumer = mock(Consumer.class);
        Consumer<String> secondStatisticsConsumer = mock(Consumer.class);

        threadPoolService = new ThreadPoolService();
        threadPoolService.setupStatisticsPrinting(STATISTICS_DELAY, firstStatisticsConsumer);

        verify(firstStatisticsConsumer, timeout(3 * STATISTICS_DELAY).atLeastOnce()).accept(anyString());
        assertThat(threadPoolService.getThreadPoolNumber(), is(1));

        // reassign statistics consumer
        threadPoolService.setupStatisticsPrinting(STATISTICS_DELAY, secondStatisticsConsumer);

        verify(secondStatisticsConsumer, timeout(3 * STATISTICS_DELAY).atLeastOnce()).accept(anyString());
        assertThat(threadPoolService.getThreadPoolNumber(), is(1)); // it means the same thread pool is used for the new sconsumer
    }

    @Test
    public void shouldShutdonwnNow() {
        threadPoolService = new ThreadPoolService();
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
        threadPoolService = new ThreadPoolService();
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
        threadPoolService = new ThreadPoolService();
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
        threadPoolService = new ThreadPoolService();
        Runnable task = mock(Runnable.class);

        ExecutorService executorService = threadPoolService.newSingleThreadExecutor(ANY_THREAD_POOL_NAME);
        executorService.submit(task);

        verify(task, timeout(STATISTICS_DELAY).times(1)).run();
    }

    @Test
    public void shouldCreateFixedExecutorService() {
        threadPoolService = new ThreadPoolService();
        Runnable task = mock(Runnable.class);

        ExecutorService executorService = threadPoolService.newFixedThreadPool(2, ANY_THREAD_POOL_NAME);
        executorService.submit(task);

        verify(task, timeout(STATISTICS_DELAY).times(1)).run();
    }

    @Test
    public void shouldCreateSingleScheduledExecutorService() {
        threadPoolService = new ThreadPoolService();
        Runnable task = mock(Runnable.class);

        ScheduledExecutorService scheduledExecutorService = createFirstScheduledThreadPool();
        scheduledExecutorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(task, timeout(3 * SHORT_DELAY).times(1)).run();
    }

    @Test
    public void shouldCreateFixedScheduledExecutorService() {
        threadPoolService = new ThreadPoolService();
        Runnable task = mock(Runnable.class);

        ScheduledExecutorService scheduledExecutorService = threadPoolService.newScheduledThreadPool(2, ANY_THREAD_POOL_NAME);
        scheduledExecutorService.schedule(task, SHORT_DELAY, TimeUnit.MILLISECONDS);

        verify(task, timeout(3 * SHORT_DELAY).times(1)).run();
    }

    private ScheduledExecutorService createFirstScheduledThreadPool() {
        return threadPoolService.newSingleScheduledThreadPool(ANY_THREAD_POOL_NAME);
    }

    private ScheduledExecutorService createSecondScheduledThreadPool() {
        return threadPoolService.newSingleScheduledThreadPool(SECOND_ANY_THREAD_POOL_NAME);
    }

}
