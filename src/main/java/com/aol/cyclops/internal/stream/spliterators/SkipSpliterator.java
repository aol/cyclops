package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class SkipSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
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


    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(index++<skip) {
            return source.tryAdvance(e->{});
        }
        return source.tryAdvance(t -> {

                action.accept(t);
            });

    }
}
