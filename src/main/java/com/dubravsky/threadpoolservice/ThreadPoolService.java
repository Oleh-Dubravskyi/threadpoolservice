package com.dubravsky.threadpoolservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ThreadPoolService {

    private final List<ExecutorService> executorServices = new ArrayList<>();
    private final Consumer<Exception> exceptionHandler;
    private final Consumer<String> statisticsHandler;
    private final ScheduledExecutorService serviceThreadPool;

    public static ThreadPoolService create() {
        return builder().build();
    }

    public static ThreadPoolServiceBuilder builder() {
        return new ThreadPoolServiceBuilder();
    }

    ThreadPoolService(Consumer<Exception> exceptionHandler, Consumer<String> statisticsHandler, long statisticsOutputDelay) {
        this.exceptionHandler = exceptionHandler;
        this.statisticsHandler = statisticsHandler;
        this.serviceThreadPool = startStatisticsPrinting(statisticsOutputDelay);
    }

    private ScheduledExecutorService startStatisticsPrinting(Long delayMillis) {
        if (delayMillis <= 0 || statisticsHandler == null) {
            return null;
        }

        ScheduledExecutorService result = newSingleScheduledThreadPool("ServicePool");
        result.scheduleAtFixedRate(this::printStatistics, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
        return result;
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
        statisticsHandler.accept(message);
    }

}
