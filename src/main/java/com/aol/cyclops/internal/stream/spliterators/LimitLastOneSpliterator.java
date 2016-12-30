package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;


public class LimitLastOneSpliterator<T> extends AbstractSpliterator<T> implements CopyableSpliterator<T>{

   
    private static final Object UNSET = new Object();
    private volatile Object buffer;
    private boolean closed = false;
    private final Spliterator<T> source;

    public LimitLastOneSpliterator(final Spliterator<T> source) {
        super(source.estimateSize(),source.characteristics());
        buffer = UNSET;
        this.source = source;
        
    }

    
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(closed)
            return false;

        source.forEachRemaining(e -> { // onNext add to buffer

            buffer = e;
        });
        if (buffer == UNSET)
            return false;
        action.accept((T) buffer);
        closed = true;
        return false;
    }

    @Override
    public Spliterator<T> copy() {
        return new LimitLastOneSpliterator<T>(CopyableSpliterator.copy(source));
    }
}
