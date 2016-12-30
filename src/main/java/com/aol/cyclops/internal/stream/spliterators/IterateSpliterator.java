package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


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
        if(current==null){
            current = in;
            action.accept(in);
            return true;
        }


        action.accept(current = fn.apply(current));
        
        return true;

    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        if(current==null) {
            current = in;
            action.accept(in);
        }
        T current2= current;
        for(;;){
            action.accept(current2 = fn.apply(current2));
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
