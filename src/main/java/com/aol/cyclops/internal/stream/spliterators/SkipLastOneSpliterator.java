package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;


public class SkipLastOneSpliterator<T> extends AbstractSpliterator<T> implements CopyableSpliterator<T>{


    private static final Object UNSET = new Object();
    private volatile Object buffer;
    private boolean closed = false;
    private final Spliterator<T> source;

    public SkipLastOneSpliterator(final Spliterator<T> source) {
        super(source.estimateSize(),source.characteristics());
        buffer = UNSET;
        this.source = source;
        
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        source.forEachRemaining(e->{
            if (buffer != UNSET){
                action.accept((T) buffer);
            }
            buffer = e;
        });
        closed = true;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(closed)
            return false;
        if (buffer != UNSET){
            action.accept((T) buffer);
        }

        boolean canAdvance = source.tryAdvance(e -> { // onNext add to buffer
            buffer = e;
        });


        return closed = canAdvance;
    }

    @Override
    public Spliterator<T> copy() {
        return new SkipLastOneSpliterator<T>(CopyableSpliterator.copy(source));
    }
}
