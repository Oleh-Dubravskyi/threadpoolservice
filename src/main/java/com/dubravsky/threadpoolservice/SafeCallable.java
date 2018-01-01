package com.dubravsky.threadpoolservice;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

class SafeCallable<T> implements Callable<T> {

    private final Callable<T> task;
    private final Consumer<Exception> exceptionHandler;

    public SafeCallable(Callable<T> task, Consumer<Exception> exceptionHandler) {
        this.task = task;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public T call() {
        try {
            return task.call();
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            }
        }
        return null;
    }

}
