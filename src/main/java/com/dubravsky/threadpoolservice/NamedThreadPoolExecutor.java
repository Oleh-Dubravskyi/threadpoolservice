package com.dubravsky.threadpoolservice;

import java.util.concurrent.ExecutorService;

public interface NamedThreadPoolExecutor extends ExecutorService {

    String getName();

}
