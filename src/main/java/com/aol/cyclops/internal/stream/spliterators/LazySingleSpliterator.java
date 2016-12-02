package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;



public class LazySingleSpliterator<T> implements Spliterator<T> {

    private final Supplier<T> array;
    
    public LazySingleSpliterator(Supplier<T> array) {
        this.array = array;
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

        action.accept(array.get());
        
        return closed =true;

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

    
}
