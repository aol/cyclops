package com.aol.cyclops.internal.stream.spliterators;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class MappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R> implements CopyableSpliterator<R> {
    Spliterator<T> source;
    Function<? super T, ? extends R> mapper;
    public MappingSpliterator(final Spliterator<T> source,Function<? super T, ? extends R> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = mapper;

    }
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        source.forEachRemaining(t->{
            action.accept(mapper.apply(t));
        });

    }

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        return source.tryAdvance(t->{
            action.accept(mapper.apply(t));
        });
    }

    @Override
    public Spliterator<R> copy() {
        return new MappingSpliterator<T, R>(CopyableSpliterator.copy(source),mapper);
    }
}
