package com.aol.cyclops.internal.stream.spliterators;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */

public class IteratableSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T>{

    private final Iterable<T> source;
    @Wither
    Iterator<T> active;

    public IteratableSpliterator(final Iterable<T> source, long size, int characteristics) {
        super(size,characteristics & Spliterator.ORDERED);

        this.source = source;


    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if(active==null)
            active = source.iterator();
        active.forEachRemaining(action);

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(active==null)
            active=source.iterator();
        if(active.hasNext())
            action.accept(active.next());
        return active.hasNext();
    }

    @Override
    public Spliterator<T> copy() {
        return new IteratableSpliterator<>(source,this.estimateSize(),this.characteristics());
    }
}
