# ThreadPoolService

ThreadPoolService is a service which simplifies usage of ExecutorServices in Java. It is written in Java 8.

With the help of ThreadPoolService you can:
 - Name a thread in ExecutorService
 - Handle exceptions thrown in any task submitted to ExecutorService
 - Get statistics of usage of the thread pool
 - Shutdown all thread pools in the single-exit point

# Usage

An ExecutorService can be created using following example:
```java
ThreadPoolService threadPoolService = ThreadPoolService.create();
ExecutorService executorService = threadPoolService.newSingleThreadExecutor("Test");
```
Actuallly, the only difference between the thread pool created by ThreadPoolService and the one created by Executors class is that ThreadPoolService gives name to all its threads using string argument. To give name of threads created by Executors class you have to create ThreadFactory instance.

## Shutdown all ExecutorService's
Also, you can shutdown all thread pools created by ThreadPoolService in one place using shutdown() method:
```java
threadPoolService.shutdown();
```

## Catch unhandled exceptions
To catch all unhandled exceptions in any task submitted to ExecutorService you have to specify exception handler:
```java
ThreadPoolService threadPoolService = ThreadPoolService.builder()
        .exceptionHandler(exception -> logger.log("Handled exception: " + exception))
        .build();

ExecutorService executorService = threadPoolService.newSingleThreadExecutor("Test");
executorService.submit(() -> {
    throw new IOException("Test Exception");
});

```

## Gathering Statistics
Also, you can periodically get the statistics of thread pool usage:
```java
ThreadPoolService threadPoolService = ThreadPoolService.builder()
        .statisticsHandler((statisticsObject, message) -> logger.log("Statistics: " + message))
        .statisticsOutputDelay(3_000)
        .build();
```

## Full featured example
And here is the sample of all features of ThreadPoolService:
```java
ThreadPoolService threadPoolService = ThreadPoolService.builder()
        .exceptionHandler(exception -> logger.log("Handled exception: " + exception))
        .statisticsHandler((statisticsObject, message) -> logger.log("Statistics: " + message))
        .statisticsOutputDelay(3_000)
        .build();

ExecutorService executorService = threadPoolService.newSingleThreadExecutor("Test");
ScheduledExecutorService scheduledExecutorService = threadPoolService.newSingleScheduledThreadPool("ScheduledTest");

scheduledExecutorService.schedule(() -> threadPoolService.shutdown(), 10, TimeUnit.SECONDS);
scheduledExecutorService.scheduleAtFixedRate(() -> logger.log("Periodic task"), 2, 2, TimeUnit.SECONDS);
executorService.execute(() -> logger.log("Task 01"));
executorService.submit(() -> {
    throw new IOException("Test Exception");
});
```

The output of this code snippet is following:
```sh
2018-01-01T23:51:50.887: Task 01
2018-01-01T23:51:50.895: Handled exception: java.io.IOException: Test Exception
2018-01-01T23:51:52.770: Periodic task
2018-01-01T23:51:53.753: Statistics: ServicePool                        Threads:   1   Active:   1   Tasks in Queue:      0   Completed Tasks:      0
2018-01-01T23:51:53.756: Statistics: Test                               Threads:   1   Active:   0   Tasks in Queue:      0   Completed Tasks:      2
2018-01-01T23:51:53.757: Statistics: ScheduledTest                      Threads:   1   Active:   0   Tasks in Queue:      2   Completed Tasks:      1
2018-01-01T23:51:54.769: Periodic task
2018-01-01T23:51:56.744: Statistics: ServicePool                        Threads:   1   Active:   1   Tasks in Queue:      0   Completed Tasks:      1
2018-01-01T23:51:56.745: Statistics: Test                               Threads:   1   Active:   0   Tasks in Queue:      0   Completed Tasks:      2
2018-01-01T23:51:56.746: Statistics: ScheduledTest                      Threads:   1   Active:   0   Tasks in Queue:      2   Completed Tasks:      2
2018-01-01T23:51:56.769: Periodic task
2018-01-01T23:51:58.769: Periodic task
2018-01-01T23:51:59.744: Statistics: ServicePool                        Threads:   1   Active:   1   Tasks in Queue:      0   Completed Tasks:      2
2018-01-01T23:51:59.745: Statistics: Test                               Threads:   1   Active:   0   Tasks in Queue:      0   Completed Tasks:      2
2018-01-01T23:51:59.746: Statistics: ScheduledTest                      Threads:   1   Active:   0   Tasks in Queue:      2   Completed Tasks:      4
```

# License

Proton is released under the MIT License. http://www.opensource.org/licenses/mit-license