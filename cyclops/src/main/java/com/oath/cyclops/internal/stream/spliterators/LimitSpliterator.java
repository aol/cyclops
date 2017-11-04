package com.oath.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class LimitSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    private final Spliterator<T> source;
    private final long take;
    long index =0;
    public LimitSpliterator(final Spliterator<T> source, long take) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.take = take;

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        if(source.hasCharacteristics(Spliterator.SIZED) && source.getExactSizeIfKnown()>0 && source.getExactSizeIfKnown()<=take) {
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

    @Override
    public Spliterator<T> copy() {
        return new LimitSpliterator<>(CopyableSpliterator.copy(source),take);
    }
}
