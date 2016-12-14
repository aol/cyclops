package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Collector;


public class CollectingSinkSpliterator<T,A,R> extends AbstractSpliterator<R> implements Runnable {
    private final Spliterator<T> s;
    private final Collector<? super T,A,R> monoid;
    volatile A total;
    volatile Consumer<? super R> action;
    public CollectingSinkSpliterator(long est, int additionalCharacteristics, Spliterator<T> s, Collector<? super T,A,R> monoid) {
        super(
              est, additionalCharacteristics & Spliterator.ORDERED);
       
        this.s=s;
        this.monoid = monoid;
        this.total =  monoid.supplier().get();
    }
    private Object lock = new Object();

    public void run(){
        action.accept(result());
    }
    
    public R result(){
        return monoid.finisher().apply(total);
    }
    
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        this.action = action;
        A res = monoid.supplier().get();
        s.forEachRemaining(t->{
             monoid.accumulator().accept(res, t);
            
         });
        synchronized(lock){
            total = monoid.combiner().apply(res, total);
        }
        return false;
    }

    
}