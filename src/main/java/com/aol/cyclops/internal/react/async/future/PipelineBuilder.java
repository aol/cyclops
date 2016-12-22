package com.aol.cyclops.internal.react.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import cyclops.function.Cacheable;
import cyclops.function.Memoize;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@Wither
public class PipelineBuilder {

    private final ExecutionPipeline builder;
    private final boolean autoOptimise; //fan out then syncrhonous there after
    private final Executor optimisingExec;
    private final boolean autoMemoize;
    private final Cacheable memoizeFactory;

    public PipelineBuilder() {
        builder = new ExecutionPipeline();
        autoOptimise = false;
        optimisingExec = null;
        autoMemoize = false;
        memoizeFactory = null;
    }

    private <T, R> Function<T, R> memoize(final Function<T, R> fn) {

        if (!autoMemoize)
            return fn;
        if (memoizeFactory == null)
            return Memoize.memoizeFunction(fn);
        return Memoize.memoizeFunction(fn, memoizeFactory);
    }

    public PipelineBuilder(final boolean autoOptimise, final Executor optimisingExec, final boolean autoMemoize, final Cacheable memoizeFactory) {
        builder = new ExecutionPipeline();
        this.autoOptimise = autoOptimise;
        this.optimisingExec = optimisingExec;
        this.autoMemoize = autoMemoize;
        this.memoizeFactory = memoizeFactory;
    }

    public <T, R> PipelineBuilder thenCompose(final Function<? super T, CompletableFuture<? extends R>> fn) {
        if (autoOptimise && builder.functionListSize() == 0)
            return thenComposeAsync(fn, optimisingExec);
        return withBuilder(builder.thenCompose((Function) memoize(fn)));
    }

    public <T, R> PipelineBuilder thenComposeAsync(final Function<? super T, CompletableFuture<? extends R>> fn, final Executor exec) {
        if (autoOptimise) {//if we already have a function present, compose with that
            if (builder.functionListSize() > 0)
                return thenCompose(fn);
        }

        return withBuilder(builder.thenComposeAsync((Function) memoize(fn), exec));

    }

    public <T, R> PipelineBuilder thenApplyAsync(final Function<T, R> fn, final Executor exec) {
        if (autoOptimise) {//if we already have a function present, compose with that
            if (builder.functionListSize() > 0)
                return thenApply(fn);
        }
        return withBuilder(builder.thenApplyAsync(memoize(fn), exec));

    }

    public <T> PipelineBuilder peek(final Consumer<? super T> c) {

        return withBuilder(builder.peek(c));

    }

    public <T, R> PipelineBuilder thenApply(final Function<? super T, ? extends R> fn) {
        if (autoOptimise && builder.functionListSize() == 0)
            return withBuilder(builder.thenApplyAsync(memoize(fn), optimisingExec));
        return withBuilder(builder.thenApply(memoize(fn)));

    }

    public <X extends Throwable, T> PipelineBuilder exceptionally(final Function<? super X, ? extends T> fn) {

        return withBuilder(builder.exceptionally(fn));
    }

    public <T, X extends Throwable> PipelineBuilder whenComplete(final BiConsumer<? super T, ? super X> fn) {
        return withBuilder(builder.whenComplete(fn));
    }

    public <T> FastFuture<T> build() {

        return new FastFuture<T>(
                                 builder.toFinalPipeline(), 0);
    }

    public PipelineBuilder onFail(final Consumer<Throwable> onFail) {
        return withBuilder(builder.onFail(onFail));
    }

    public boolean isSequential() {
        return builder.isSequential();
    }
}