package com.dubravsky.threadpoolservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SafeScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements NamedThreadPoolExecutor{

    private final String name;
    private Consumer<Exception> exceptionHandler;

    public SafeScheduledThreadPoolExecutor(int corePoolSize, String threadName) {
        super(corePoolSize, NamedThreadFactory.of(threadName));
        this.name = threadName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return super.schedule(safeRunnable(task), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
        return super.schedule(safeCallable(task), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(safeRunnable(task), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(safeRunnable(task), initialDelay, delay, unit);
    }

    @Override
    public void execute(Runnable task) {
        super.execute(safeRunnable(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(safeRunnable(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(safeRunnable(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(safeCallable(task));
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
        return super.newTaskFor(safeRunnable(task), value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        return super.newTaskFor(safeCallable(task));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return super.invokeAny(safeCallables(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return super.invokeAny(safeCallables(tasks), timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return super.invokeAll(safeCallables(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return super.invokeAll(safeCallables(tasks), timeout, unit);
    }

    private SafeRunnable safeRunnable(Runnable task) {
        return new SafeRunnable(task, exceptionHandler);
    }

    private <T> SafeCallable safeCallable(Callable<T> task) {
        return new SafeCallable<>(task, exceptionHandler);
    }

    private <T> Collection<? extends Callable<T>> safeCallables(Collection<? extends Callable<T>> tasks) {
        Collection<SafeCallable<T>> result = new ArrayList<>();
        for (Callable<T> task : tasks) {
            result.add(new SafeCallable<>(task, exceptionHandler));
        }
        return result;
    }

}
