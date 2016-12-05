package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;



public class FillSpliterator<T> implements Spliterator<T> {

    private final T value;
    
 

    public FillSpliterator(T value) {
        super();
        this.value = value;
    }

    @Override
    public long estimateSize() {
        return 1l;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }


    private boolean open = true;
    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
       if(open)
        action.accept(value);
        
       return open =false;

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

    
}
