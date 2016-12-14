package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.aol.cyclops.Monoid;

import lombok.Getter;

public class FoldingSinkSpliterator<T> extends AbstractSpliterator<T>implements Runnable{
    private final Spliterator<T> s;
    private final Monoid<T> monoid;
    
    volatile Consumer<? super T> action;
    volatile T total; //sync only execution for now
    public FoldingSinkSpliterator(long est, int additionalCharacteristics, Spliterator<T> s, Monoid<T> monoid) {
        super(
              est, additionalCharacteristics);
       
        this.s=s;
        this.monoid = monoid;
        total = monoid.zero();
    }

    public void run(){
        System.out.println("Setting " +total);
        action.accept(total);
    }
   
    
    
    /* (non-Javadoc)
     * @see java.util.Spliterator#forEachRemaining(java.util.function.Consumer)
     */
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
       System.out.println("Woot!");
       tryAdvance(action);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        this.action = action;
        Object[] acc = {monoid.zero()};
        
        s.forEachRemaining(t->{
            
             total = monoid.apply(total,t);
            
            
         });
       
        return false;
    }

    
}
