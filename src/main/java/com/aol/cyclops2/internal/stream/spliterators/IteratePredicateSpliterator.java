package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;


public class IteratePredicateSpliterator<T> implements Spliterator<T>, CopyableSpliterator<T> {

    private final T in;
    private final UnaryOperator<T> fn;
    private final Predicate<? super T> pred;

    public IteratePredicateSpliterator(T in, UnaryOperator<T>fn, Predicate<? super T> pred) {
        this.in = in;
        this.fn = fn;
        this.pred=pred;

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


        current = (current!=null ? fn.apply(current) : in);
        if(pred.test(current))
            action.accept(current);
        else
            return false;
        
        return true;

    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        for(;;){
            current = (current!=null ? fn.apply(current) : in);
            if(pred.test(current))
                action.accept(current);
            else
                return;
        }
    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }


    @Override
    public Spliterator<T> copy() {
        return new IteratePredicateSpliterator<>(in,fn,pred);
    }
}
