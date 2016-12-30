package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class SkipSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    Spliterator<T> source;
    long skip;
    long index =0;
    public SkipSpliterator(final Spliterator<T> source,long skip) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.skip = skip;

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        for(;index<skip;index++){
            source.tryAdvance(e->{});

        }

        source.forEachRemaining(action);
        closed = true;

    }
    boolean closed = false;

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(closed)
            return false;
        boolean cont = true;
        while(index++<skip && cont) {
            cont =source.tryAdvance(e->{});

        }
        if(!cont)
            return closed = false;
        return closed = source.tryAdvance(t -> {

                action.accept(t);
            });

    }

    @Override
    public Spliterator<T> copy() {
        return new SkipSpliterator<T>(CopyableSpliterator.copy(source),skip);
    }
}
