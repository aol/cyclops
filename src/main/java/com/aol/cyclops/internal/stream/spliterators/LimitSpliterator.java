package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class LimitSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
    Spliterator<T> source;
    long take;
    long index =0;
    public LimitSpliterator(final Spliterator<T> source, long take) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.take = take;

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if(source.getExactSizeIfKnown()<=take) {
            source.forEachRemaining(action);
            return;
        }
        //use local index
        for(long index = this.index; index<take;index++){
            source.tryAdvance(t -> {

                action.accept(t);
            });
        }

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(index<take) {

            return source.tryAdvance(t -> {
                index++;
                action.accept(t);
            });
        }
        return false;
    }
}
