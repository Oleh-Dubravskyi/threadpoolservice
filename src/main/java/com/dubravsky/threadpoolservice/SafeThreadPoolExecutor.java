package com.dubravsky.threadpoolservice;

import com.dubravsky.threadpoolservice.task.SafeCallable;
import com.dubravsky.threadpoolservice.task.SafeRunnable;
import com.dubravsky.threadpoolservice.util.NamedThreadFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

class SafeThreadPoolExecutor extends ThreadPoolExecutor implements NamedThreadPoolExecutor {

    private final String name;
    private Consumer<Exception> exceptionHandler;

    public SafeThreadPoolExecutor(int nThreads, String threadName) {
        super(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                NamedThreadFactory.of(threadName));
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

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable task, T value) {
        return super.newTaskFor(safeRunnable(task), value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        return super.newTaskFor(safeCallable(task));
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
