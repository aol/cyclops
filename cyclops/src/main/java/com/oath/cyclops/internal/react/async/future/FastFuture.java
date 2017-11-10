package com.oath.cyclops.internal.react.async.future;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.internal.react.exceptions.SimpleReactCompletionException;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * @author John McClean
 * assumptions
 * 1. join is read once
 * 2. only transform / mapAsync/ exceptionally/ allOf and anyOf are neccessary
 * 3. For results / errors : single writer (one thread executing a task at a time, one thread sets the result or error)
 * 						/ single reader (simple-react Stream)
 * 4. For post-hoc event listeners : single writer (simple-react Stream adds event listeners) : single reader (only one thread can read event listeners -
 * 						lazy the thread that sets the result / error and eventually done,
 * 							or if done already set - the calling thread can execute post-hoc events)
 */
@AllArgsConstructor
public class FastFuture<T> {

    @Getter
    private volatile boolean done = false;
    private volatile Consumer<OnComplete> forXOf;
    private volatile Consumer<OnComplete> essential;
    @Getter
    private volatile boolean completedExceptionally = false;
    private final AtomicReference result = new AtomicReference(
                                                               UNSET);
    private final AtomicReference exception = new AtomicReference(
                                                                  UNSET);
    private final Consumer<FastFuture<T>> doFinally;
    private static UnSet UNSET = new UnSet();

    static class UnSet {
    }

    @Getter
    private final FinalPipeline pipeline;

    private final AtomicInteger count = new AtomicInteger(
                                                          0);
    private final AtomicInteger max = new AtomicInteger(
                                                        0);

    public FastFuture() {
        max.set(0);
        this.doFinally = null;
        this.pipeline = null;
    }

    private T result() {
        Object res = UNSET;
        while ((res = result.get()) == UNSET) {
            Thread.yield();
        }
        return (T) res;
    }

    private Throwable exception() {
        Object result = UNSET;
        while ((result = exception.get()) == UNSET) {
            Thread.yield();
        }
        return (Throwable) result;
    }

    public FastFuture(final FinalPipeline pipeline, final Consumer<FastFuture<T>> doFinally) {
        this.max.set(0);
        this.pipeline = pipeline;
        this.doFinally = doFinally;

    }

    public FastFuture(final FinalPipeline pipeline, final int max) {
        this.max.set(max);
        this.pipeline = pipeline;
        this.doFinally = null;
    }

    public void await() {

        long spin = 1;
        while (!done) {
            LockSupport.parkNanos(spin++);
        }

    }

    /**
     * Join which can be called exactly once!
     *
     * @return Result
     */
    public T join() {

        try {
            long spin = 1;
            while (!done) {
                LockSupport.parkNanos(spin++);
            }
            if (completedExceptionally)
                throw new SimpleReactCompletionException(
                                                         exception());
            return result();
        } finally {
            markComplete();
        }
    }

    public void markComplete() {
        if (doFinally != null)
            doFinally.accept(this);
    }

    public static <T> FastFuture<T> completedFuture(final T value) {
        final FastFuture<T> f = new FastFuture();
        f.result.lazySet(value);
        f.done = true;
        return f;
    }

    public CompletableFuture<T> toCompletableFuture() {
        final CompletableFuture<T> f = new CompletableFuture<>();
        final AtomicInteger count = new AtomicInteger(
                                                      0);
        this.onComplete(c -> {
            if (!count.compareAndSet(0, 1))
                return;
            if (c.exceptionally)
                f.completeExceptionally(c.exception);
            else
                f.complete((T) c.result);
        });
        return f;

    }

    public FastFuture<T> populateFromCompletableFuture(final CompletableFuture<T> cf) {
        cf.thenAccept(i -> this.set(i));
        cf.exceptionally(t -> {
            completedExceptionally(t);
            return join();
        });
        return this;
    }

    private void completedExceptionally(final Throwable t) {

        Throwable finalError = t;
        for (int i = 0; i < this.pipeline.firstRecover.length; i++) {
            try {
                this.set((T) pipeline.firstRecover[i].apply(t));
                return;

            } catch (final Throwable e) {
                finalError = e;
                this.exception.lazySet(e);
            }
        }
        this.completeExceptionally(finalError);

        throw (RuntimeException) exception();
    }

    private FastFuture<T> completeExceptionally(final Throwable t) {
        exception.lazySet(t);
        completedExceptionally = true;
        handleOnComplete(true);
        if (pipeline != null && pipeline.onFail != null)
            pipeline.onFail.accept(t);
        done = true;
        return this;
    }

    public static <T> FastFuture<T> failedFuture(final Throwable t) {
        return new FastFuture<T>().completeExceptionally(t);
    }

    /** Internal conversion method to convert CompletableFutures to FastFuture.
     */
    public static <T> FastFuture<T> fromCompletableFuture(final CompletableFuture<T> cf) {
        final FastFuture<T> f = new FastFuture<>();
        cf.thenAccept(i -> f.set(i));
        cf.exceptionally(t -> {

            f.completedExceptionally(t);
            return f.join();
        });
        return f;
    }

    public static <R> FastFuture<List<R>> allOf(final Runnable onComplete, final FastFuture... futures) {
        //needs to use onComplete
        final FastFuture allOf = new FastFuture(
                                                FinalPipeline.empty(), futures.length);

        for (final FastFuture next : futures) {
            final AtomicInteger count = new AtomicInteger(
                                                          0);
            next.onComplete(v -> {
                if (!count.compareAndSet(0, 1))
                    return;
                if (allOf.count.incrementAndGet() == allOf.max.get()) {
                    onComplete.run();
                }

            });
        }
        return allOf;
    }

    public static <R> FastFuture<List<R>> xOf(final int x, final Runnable onComplete, final FastFuture... futures) {
        //needs to use onComplete
        final FastFuture xOf = new FastFuture(
                                              FinalPipeline.empty(), x);
        for (final FastFuture next : futures) {
            final AtomicInteger count = new AtomicInteger(
                                                          0);
            next.onComplete(v -> {
                if (!count.compareAndSet(0, 1))
                    return;
                if (xOf.count.incrementAndGet() >= xOf.max.get()) {

                    onComplete.run();

                }

            });
        }
        return xOf;
    }

    public static <R> FastFuture<List<R>> anyOf(final FastFuture... futures) {

        final FastFuture anyOf = new FastFuture();

        for (final FastFuture next : futures) {
            next.onComplete(v -> {
                anyOf.result.lazySet(true);
                anyOf.done();

            });
        }
        return anyOf;
    }

    public void set(final T result) {

        try {

            final Object use = result;

            if (pipeline == null || pipeline.functions.length == 0) {
                this.result.lazySet(use);
                done();
                return;
            }
            final Function op = pipeline.functions[0];
            if (this.pipeline.executors[0] != null) {

                this.pipeline.executors[0].execute(() -> {
                    set(() -> (T) op.apply(use), 1);
                });

            } else {

                set(() -> (T) op.apply(use), 1);

            }
        } catch (final Throwable t) {

            completeExceptionally(t);
        }

    }

    private void set(final Supplier<T> result, final int index) {
        try {

            final Object current = result.get();

            final Object use = current;
            if (index < pipeline.functions.length) {
                final Function op = pipeline.functions[index];
                this.pipeline.executors[index].execute(() -> {
                    set(() -> (T) op.apply(use), index + 1);
                });
                return;
            }

            this.result.lazySet(current);
            done();

        } catch (final Throwable t) {
            if (t instanceof CompletedException) {
                if (this.doFinally != null)
                    doFinally.accept(this);
            }

            completeExceptionally(t);

        }

    }

    private boolean done() {
        this.completedExceptionally = false;
        this.done = true;
        handleOnComplete(true);

        return true;

    }

    public void clearFast() {
        result.set(UNSET);
        exception.set(UNSET);
        this.forXOf = null;
        this.essential = null;
        this.count.set(0);
        this.max.set(0);
        this.completedExceptionally = false;
        this.done = false;
    }

    /**
     * Called at least once on complete
     *
     */
    public void essential(final Consumer<OnComplete> fn) {
        this.essential = fn; //set - could also be called on a separate thread
        if (done) { //can be called again
            fn.accept(buildOnComplete());
        }
    }

    /**
     * Called at least once on complete
     *
     */
    public void onComplete(final Consumer<OnComplete> fn) {

        this.forXOf = fn; //set - could also be called on a separate thread

        if (done) { //can be called again
            fn.accept(buildOnComplete());
        }
    }

    private void handleOnComplete(final boolean force) {
        if (forXOf != null)
            forXOf.accept(buildOnComplete());

        if (this.essential != null)
            this.essential.accept(buildOnComplete());

    }

    private OnComplete buildOnComplete() {
        final OnComplete c = new OnComplete(
                                            !completedExceptionally && done ? result() : null, completedExceptionally ? exception() : null,
                                            this.completedExceptionally);
        return c;
    }

    @AllArgsConstructor
    public static class OnComplete {
        public final Object result;
        public final Throwable exception;
        public final boolean exceptionally;
    }

}
