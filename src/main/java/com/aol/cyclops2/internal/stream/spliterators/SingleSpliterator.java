package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;


public class SingleSpliterator<T> implements Spliterator<T>, CopyableSpliterator<T> {

    private final T in;


    public SingleSpliterator(T in) {
        this.in = in;

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
    public boolean tryAdvance(final Consumer<? super T> action) {
        if(closed)
            return false;

        action.accept(in);
        
        return closed =true;

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }


    @Override
    public Spliterator<T> copy() {
        return new SingleSpliterator<>(in);
    }
}
