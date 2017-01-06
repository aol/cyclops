package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClosingSpliterator<IN,T> extends BaseComposableSpliterator<IN,T,ClosingSpliterator<IN,?>> implements Spliterator<T> {
    private final long estimate;

    private final Queue<IN> queue;
    private final AtomicBoolean open;

    public ClosingSpliterator(final long estimate, final Queue queue, final AtomicBoolean open) {
        super(estimate, ORDERED,null);
        this.estimate = estimate;
        this.open = open;
        this.queue = queue;

    }
    ClosingSpliterator(Function<? super IN, ? extends T> fn,final long estimate, final Queue queue, final AtomicBoolean open) {
        super(estimate, ORDERED,fn);
        this.estimate = estimate;
        this.open = open;
        this.queue = queue;


    }

    @Override
    public long estimateSize() {
        return estimate;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        Objects.requireNonNull(action);
        final Consumer<? super IN> toUse = apply(action);

        if (!open.get() && queue.size() == 0) {

            return false;
        }

        while (open.get() || queue.size() > 0) {
            long nanos = 1l;

            IN value;
            if ((value = queue.poll()) != null) {
                toUse.accept(nullSafe(value));

                return true;
            }
            LockSupport.parkNanos(nanos);
            nanos = nanos * 2;

        }
        return false;

    }

    private IN nullSafe(final IN value) {
        return value;
    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

    @Override
    <R2> ClosingSpliterator<IN, ?> create(Function<? super IN, ? extends R2> after) {
        return new ClosingSpliterator(fn,estimate,queue,open);
    }
}
