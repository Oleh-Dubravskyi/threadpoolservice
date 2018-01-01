package com.dubravsky.threadpoolservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ThreadPoolService {

    private final List<ExecutorService> executorServices = new ArrayList<>();
    private Consumer<String> statisticsConsumer;
    private Consumer<Exception> exceptionHandler;
    private ScheduledExecutorService serviceThreadPool;
    private ScheduledFuture<?> printStatisticsFeature;

    public void setupStatisticsPrinting(Long delayMillis, Consumer<String> statisticsConsumer) {
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("To print thread pool statistics delay should be positive but it is " + delayMillis);
        }
        if (statisticsConsumer == null) {
            throw new IllegalArgumentException("To print thread pool statistics consumer should be not null");
        }

        this.statisticsConsumer = statisticsConsumer;

        if (serviceThreadPool == null) {
            serviceThreadPool = newSingleScheduledThreadPool("ServicePool");
        }

        if (printStatisticsFeature != null) {
            printStatisticsFeature.cancel(false);
        }
        printStatisticsFeature = serviceThreadPool.scheduleAtFixedRate(this::printStatistics, delayMillis,
                delayMillis, TimeUnit.MILLISECONDS);
    }

    public void setExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public int getThreadPoolNumber() {
        return executorServices.size();
    }

    public ExecutorService newSingleThreadExecutor(String threadName) {
        return newFixedThreadPool(1, threadName);
    }

    public ExecutorService newFixedThreadPool(int nThreads, String threadName) {
        SafeThreadPoolExecutor executorService = new SafeThreadPoolExecutor(nThreads, threadName);
        executorService.setExceptionHandler(exceptionHandler);
        add(executorService);
        return executorService;
    }

    public ScheduledExecutorService newSingleScheduledThreadPool(String threadName) {
        return newScheduledThreadPool(1, threadName);
    }

    public ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String threadName) {
        SafeScheduledThreadPoolExecutor scheduledExecutorService = new SafeScheduledThreadPoolExecutor(corePoolSize, threadName);
        scheduledExecutorService.setExceptionHandler(exceptionHandler);
        add(scheduledExecutorService);
        return scheduledExecutorService;
    }

    public void shutdown() {
        for (ExecutorService executorService : executorServices) {
            executorService.shutdown();
        }
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks = new ArrayList<>();
        for (ExecutorService executorService : executorServices) {
            tasks.addAll(executorService.shutdownNow());
        }
        return tasks;
    }

    public boolean isShutdown() {
        for (ExecutorService executorService : executorServices) {
            if (!executorService.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    public boolean isTerminated() {
        for (ExecutorService executorService : executorServices) {
            if (!executorService.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    private void add(ExecutorService executorService) {
        executorServices.add(executorService);
    }

    private void printStatistics() {
        executorServices.forEach(executorService -> printStatistics((ThreadPoolExecutor) executorService));
    }

    private void printStatistics(ThreadPoolExecutor threadPoolExecutor) {
        String name = ((NamedThreadPoolExecutor) threadPoolExecutor).getName();
        String message = String.format("%-32s   Threads: %3d   Active: %3d   Tasks in Queue: %6d   Completed Tasks: %6d",
                name,
                threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getCompletedTaskCount());
        statisticsConsumer.accept(message);
    }

}
