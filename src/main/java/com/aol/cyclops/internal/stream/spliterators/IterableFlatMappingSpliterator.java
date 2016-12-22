package com.aol.cyclops.internal.stream.spliterators;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class IterableFlatMappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R> implements CopyableSpliterator<R> {
    Spliterator<T> source;
    Function<? super T, ? extends Iterable<R>> mapper;
    public IterableFlatMappingSpliterator(final Spliterator<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = (Function<? super T, ? extends Iterable<R>>)mapper;

    }
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        source.forEachRemaining(t->{
            Iterable<R> flatten = mapper.apply(t);
            flatten.forEach(action);
        });

    }

    Iterator<R> active;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        if(active!=null && active.hasNext()){
            action.accept(active.next());
            return active.hasNext();
        }
        source.tryAdvance(t->{
            if(active==null || !active.hasNext()) {
                active = mapper.apply(t).iterator();
            }
            if(active.hasNext())
                action.accept(active.next());


        });
        return active.hasNext();
    }

    @Override
    public Spliterator<R> copy() {
        return new IterableFlatMappingSpliterator<>(source,mapper);
    }
}
