package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;



public class LazySingleSpliterator<T, X extends Stream<T>,R> implements Spliterator<R> {

    private final X in;
    private final Function<? super X,? extends R> fn;
    
    public LazySingleSpliterator( X in,Function<? super X,? extends R> fn) {
        this.in = in;
        this.fn = fn;
    }

    @Override
    public long estimateSize() {
        return 1l;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }

    

    private boolean closed = false;
    @Override
    public boolean tryAdvance(final Consumer<? super R> action) {
        if(closed)
            return false;

        action.accept(fn.apply(in));
        
        return closed =true;

    }

    @Override
    public Spliterator<R> trySplit() {

        return this;
    }

    
}
