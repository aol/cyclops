package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;


public class IterateSpliterator<T> implements Spliterator<T>, CopyableSpliterator<T> {

    private final T in;
    private final UnaryOperator<T> fn;

    public IterateSpliterator(T in, UnaryOperator<T>fn) {
        this.in = in;
        this.fn = fn;

    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }

    

    private T current;
    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {



        action.accept(current = (current!=null ? fn.apply(current) : in));
        
        return true;

    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        for(;;){
            action.accept(current = (current!=null ? fn.apply(current) : in));
        }
    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }


    @Override
    public Spliterator<T> copy() {
        return new IterateSpliterator<>(in,fn);
    }
}
