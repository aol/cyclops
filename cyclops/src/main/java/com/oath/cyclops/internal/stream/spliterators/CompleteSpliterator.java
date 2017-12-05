package com.oath.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by wyang14 on 19/07/2017.
 */

public class CompleteSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {

    private final Spliterator<T> source;
    private final Runnable fn;

    public CompleteSpliterator(final Spliterator<T> source, Runnable fn) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.fn = fn;

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        boolean existed = source.tryAdvance(action);
        if (!existed) {
            if (fn != null)
                fn.run();
        }
        return existed;
    }

    @Override
    public Spliterator<T> copy() {
        return new CompleteSpliterator<>(CopyableSpliterator.copy(source), fn);
    }
}
