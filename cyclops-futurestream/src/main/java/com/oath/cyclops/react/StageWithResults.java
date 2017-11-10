package com.oath.cyclops.react;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import com.oath.cyclops.types.futurestream.ConfigurableStream;
import com.oath.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class StageWithResults<RS, U> {

    private final Executor taskExecutor;

    private final ConfigurableStream<U, Object> stage;
    @Getter
    private final RS results;

    public StageWithResults(final ConfigurableStream<U, Object> stage, final RS results) {

        this.taskExecutor = stage.getTaskExecutor();
        this.stage = stage;
        this.results = results;
    }

    /**
     * This method allows the SimpleReact Executor to be reused by JDK parallel streams. It is best used when
     * collectResults and block are called explicitly for finer grained control over the blocking conditions.
     *
     * @param fn Function that contains parallelStream code to be executed by the SimpleReact ForkJoinPool (if configured)
     */
    public <R> R submit(final Function<RS, R> fn) {
        return submit(() -> fn.apply(this.results));
    }

    /**
     * This method allows the SimpleReact Executor to be reused by JDK parallel streams
     *
     * @param callable that contains code
     */
    public <T> T submit(final Callable<T> callable) {
        if (taskExecutor instanceof ForkJoinPool) {
            try {

                return ((ForkJoinPool) taskExecutor).submit(callable)
                                                    .get();
            } catch (final ExecutionException e) {
                throw ExceptionSoftener.throwSoftenedException(e);

            } catch (final InterruptedException e) {
                Thread.currentThread()
                      .interrupt();
                throw ExceptionSoftener.throwSoftenedException(e);

            }
        }
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RuntimeException(
                                       e);
        }
    }

}
