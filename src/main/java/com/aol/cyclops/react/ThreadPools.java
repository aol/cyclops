package com.aol.cyclops.react;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

import lombok.Getter;

public class ThreadPools {
    @Getter
    private static final Executor commonFreeThread = Executors.newFixedThreadPool(1);

    @Getter
    private static final Executor currentThreadExecutor = (Runnable r) -> r.run();

    @Getter
    private static final Executor queueCopyExecutor = Executors.newFixedThreadPool(1);

    @Getter
    private static final Executor commonLazyExecutor = new ForkJoinPool(
                                                                        1);

    @Getter
    private static final ScheduledExecutorService commonFreeThreadRetry = Executors.newScheduledThreadPool(1);

    @Getter
    private static final ScheduledExecutorService commonStanardRetry = Executors.newScheduledThreadPool(Runtime.getRuntime()
                                                                                                               .availableProcessors());

    public static enum ExecutionMode {
        CURRENT,
        COMMON_FREE,
        NEW_FREE
    }

    private static volatile boolean useCommon = true;

    public static Executor getStandard() {
        if (useCommon)
            return ForkJoinPool.commonPool();
        return new ForkJoinPool(
                                Runtime.getRuntime()
                                       .availableProcessors());
    }

    public static Executor getSequential() {
        if (useCommon)
            return commonFreeThread;
        else
            return new ForkJoinPool(
                                    1);
    }

    public static ScheduledExecutorService getSequentialRetry() {
        if (useCommon)
            return commonFreeThreadRetry;
        else
            return Executors.newScheduledThreadPool(1);
    }

    public static ScheduledExecutorService getStandardRetry() {
        if (useCommon)
            return commonStanardRetry;
        else
            return Executors.newScheduledThreadPool(Runtime.getRuntime()
                                                           .availableProcessors());
    }

    public static Executor getLazyExecutor() {
        if (useCommon)
            return commonLazyExecutor;
        else
            return new ForkJoinPool(
                                    1);
    }

    public static boolean isUseCommon() {
        return useCommon;
    }

    public static void setUseCommon(boolean useCommon) {
        ThreadPools.useCommon = useCommon;
    }
}