package com.htwz.skill.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 核心线程
 *
 * @author wy
 */
public class ThreadPoolUtil {

    /**
     * 线程缓冲队列
     */
    private static BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<>(100);

    /**
     * 核心线程数，会一直存活，即使没有任务，线程池也会维护线程的最少数量
     */
    private static final int SIZE_CORE_POOL = 5;
    /**
     * 线程池维护线程的最大数量
     */
    private static final int SIZE_MAX_POOL = 10;
    /**
     * 线程池维护线程所允许的空闲时间
     */
    private static final long ALIVE_TIME = 2000;

    private static final ThreadFactory THREADFACTORY = new ThreadFactoryBuilder()
            .setNameFormat("sync-resource-%d")
            .setDaemon(true)
            .build();

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL, ALIVE_TIME,
            TimeUnit.MILLISECONDS, bqueue, THREADFACTORY, new ThreadPoolExecutor.CallerRunsPolicy());

    static {
        pool.prestartAllCoreThreads();
    }

    public static ThreadPoolExecutor getPool() {
        return pool;
    }
}
