package com.dubravsky.threadpoolservice;

import java.util.function.Consumer;

public class ThreadPoolServiceBuilder {

    private static final long DEFAULT_STATISTICS_OUTPUT_DELAY = 10_000L;

    private Consumer<Exception> exceptionHandler;
    private Consumer<String> statisticsHandler;
    private long statisticsOutputDelay = DEFAULT_STATISTICS_OUTPUT_DELAY;

    ThreadPoolServiceBuilder() {
    }

    public ThreadPoolServiceBuilder exceptionHandler(Consumer<Exception> exceptionHandler) {
        if (exceptionHandler == null) {
            throw new IllegalArgumentException("ExceptionHandler should not be null");
        }
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public ThreadPoolServiceBuilder statisticsHandler(Consumer<String> statisticsHandler) {
        if (statisticsHandler == null) {
            throw new IllegalArgumentException("To print thread pool statistics consumer should not be null");
        }
        this.statisticsHandler = statisticsHandler;
        return this;
    }

    public ThreadPoolServiceBuilder statisticsOutputDelay(long statisticsOutputDelay) {
        if (statisticsOutputDelay <= 0) {
            throw new IllegalArgumentException("Statistics delay should be positive but it is " + statisticsOutputDelay);
        }
        this.statisticsOutputDelay = statisticsOutputDelay;
        return this;
    }

    public ThreadPoolService build() {
        return new ThreadPoolService(exceptionHandler, statisticsHandler, statisticsOutputDelay);
    }

}
