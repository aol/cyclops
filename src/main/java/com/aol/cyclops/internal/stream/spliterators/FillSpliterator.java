package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;



public class FillSpliterator<T> implements Spliterator<T> {

    private final T value;
    
    private static final int characteristics = IMMUTABLE & SIZED;

    public FillSpliterator(T value) {
        super();
        this.value = value;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }



    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {

        action.accept(value);
        
       return true;

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

    
}
