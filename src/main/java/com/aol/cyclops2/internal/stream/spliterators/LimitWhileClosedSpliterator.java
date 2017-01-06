package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class LimitWhileClosedSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    private final Spliterator<T> source;
    private final Predicate<? super T> predicate;
    boolean closed = false;
    public LimitWhileClosedSpliterator(final Spliterator<T> source, Predicate<? super T> predicate) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.predicate = predicate;

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        while(!closed){
            boolean canAdvance = source.tryAdvance(t -> {
                closed = !predicate.test(t);
                action.accept(t);
            });
            if(!canAdvance){
                closed = true;
                return;
            }

        }


    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(closed)
            return true;
        boolean canAdvance = source.tryAdvance(t -> {
                closed = !predicate.test(t);
                action.accept(t);
            });

        return canAdvance && !closed;
    }

    @Override
    public Spliterator<T> copy() {
        return new LimitWhileClosedSpliterator<>(CopyableSpliterator.copy(source),predicate);
    }
}
