package com.dubravsky.threadpoolservice;

import java.util.concurrent.ThreadPoolExecutor;

public class StatisticsObject {

    private final String name;
    private final int poolSize;
    private final int activeCount;
    private final int queueSize;
    private final long completedTaskCount;

    public static StatisticsObject of(ThreadPoolExecutor threadPoolExecutor) {
        return new StatisticsObject(
                ((NamedThreadPoolExecutor) threadPoolExecutor).getName(),
                threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getCompletedTaskCount());
    }

    private StatisticsObject(String name, int poolSize, int activeCount, int queueSize, long completedTaskCount) {
        this.name = name;
        this.poolSize = poolSize;
        this.activeCount = activeCount;
        this.queueSize = queueSize;
        this.completedTaskCount = completedTaskCount;
    }

    public String getName() {
        return name;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    @Override
    public String toString() {
        return String.format("%-32s   Threads: %3d   Active: %3d   Tasks in Queue: %6d   Completed Tasks: %6d",
                getName(),
                getPoolSize(),
                getActiveCount(),
                getQueueSize(),
                getCompletedTaskCount());
    }
}
