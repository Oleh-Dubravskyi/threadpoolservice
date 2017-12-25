package com.dubravsky.threadpoolservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ThreadPoolService {

    private final List<ExecutorService> executorServices = new ArrayList<>();
    private Consumer<String> statisticsConsumer;
    private ScheduledExecutorService serviceThreadPool;

    public void setupStatisticsPrinting(Long delayMillis, Consumer<String> statisticsConsumer) {
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("To print thread pool statistics delay should be positive but it is " + delayMillis);
        }
        if (statisticsConsumer == null) {
            throw new IllegalArgumentException("To print thread pool statistics consumer should be not null");
        }

        this.statisticsConsumer = statisticsConsumer;

        serviceThreadPool = newSingleScheduledThreadPool("ThreadPoolService");
        serviceThreadPool.scheduleAtFixedRate(this::printStatistics, delayMillis,
                delayMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledExecutorService newSingleScheduledThreadPool(String threadName) {
        return newScheduledThreadPool(1, threadName);
    }

    public ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String threadName) {
        ThreadFactory threadFactory = new NamedThreadFactory(threadName);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(corePoolSize, threadFactory);
        add(scheduledExecutorService);
        return scheduledExecutorService;
    }

    public void shutdown() {
        for (ExecutorService executorService : executorServices) {
            executorService.shutdown();
        }
    }

    private void add(ExecutorService executorService) {
        executorServices.add(executorService);
    }

    private void printStatistics() {
        executorServices.forEach(executorService -> printStatistics((ThreadPoolExecutor) executorService));
    }

    private void printStatistics(ThreadPoolExecutor threadPoolExecutor) {
        String message = String.format("Total Threads: %d, Active Threads: %d, Queue Size: %d, Completed Tasks: %d",
                threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getCompletedTaskCount());
        statisticsConsumer.accept(message);
    }

}
