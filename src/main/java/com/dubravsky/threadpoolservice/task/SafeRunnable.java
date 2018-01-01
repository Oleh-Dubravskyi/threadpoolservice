package com.dubravsky.threadpoolservice.task;

import java.util.function.Consumer;

public class SafeRunnable implements Runnable {

    private final Runnable task;
    private final Consumer<Exception> exceptionHandler;

    public SafeRunnable(Runnable task, Consumer<Exception> exceptionHandler) {
        this.task = task;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            task.run();
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            }
        }
    }

}
