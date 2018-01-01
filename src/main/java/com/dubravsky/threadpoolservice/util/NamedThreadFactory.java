package com.dubravsky.threadpoolservice.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class NamedThreadFactory implements ThreadFactory {

    private final AtomicLong threadIndex = new AtomicLong(0);
    private final String threadName;

    public static ThreadFactory of(String threadName) {
        return new NamedThreadFactory(threadName);
    }

    private NamedThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(threadName + "-" + threadIndex.getAndIncrement());
        return thread;
    }

}
