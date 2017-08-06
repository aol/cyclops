package com.aol.cyclops2.react;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

import lombok.Getter;

public class ThreadPools {
    @Getter
    private static final Executor commonFreeThread = Executors.newFixedThreadPool(1);

    @Getter
    private static final Executor currentThreadExecutor = (final Runnable r) -> r.run();

    @Getter
    private static final Executor queueCopyExecutor = Executors.newFixedThreadPool(1);

    @Getter
    private static final Executor commonLazyExecutor = new ForkJoinPool(
                                                                        1);


    @Getter
    private static final ScheduledExecutorService commonSchedular = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    @Getter
    private static final ScheduledExecutorService commonSequentialSchedular = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public static enum ExecutionMode {
        CURRENT,
        COMMON_FREE,
        NEW_FREE
    }

    private static volatile boolean useCommon = true;

    /**
     * @return Standard Parallel Executor, uses the ForkJoin Common Pool is @see {@link ThreadPools#isUseCommon()} is true
     *         Otherwise a new Executor sized to the number of threads is used.
     */
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



    public static ScheduledExecutorService getSequentialSchedular() {
        if (useCommon)
            return commonSequentialSchedular;
        else
            return Executors.newScheduledThreadPool(1);
    }

    public static ScheduledExecutorService getStandardSchedular() {
        if (useCommon)
            return commonSchedular;
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

    public static void setUseCommon(final boolean useCommon) {
        ThreadPools.useCommon = useCommon;
    }
}