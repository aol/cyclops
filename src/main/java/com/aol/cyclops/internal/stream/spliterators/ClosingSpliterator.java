package com.aol.cyclops.internal.stream.spliterators;

import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class ClosingSpliterator<T> implements Spliterator<T> {
    private final long estimate;

    private final Queue<T> queue;
    private final AtomicBoolean open;

    public ClosingSpliterator(final long estimate, final Queue queue, final AtomicBoolean open) {
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

        if (!open.get() && queue.size() == 0) {

            return false;
        }

        while (open.get() || queue.size() > 0) {
            long nanos = 1l;

            T value;
            if ((value = queue.poll()) != null) {
                action.accept(nullSafe(value));

                return true;
            }
            LockSupport.parkNanos(nanos);
            nanos = nanos * 2;

        }
        return false;

    }

    private T nullSafe(final T value) {
        return value;
    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

}
