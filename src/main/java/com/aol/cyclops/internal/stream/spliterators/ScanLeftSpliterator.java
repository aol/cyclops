package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jooq.lambda.Seq;

import lombok.AllArgsConstructor;


public class ScanLeftSpliterator<T,U> implements Spliterator<U>{

    private final Spliterator<T> source;
    private U current; 
    private final BiFunction<? super U, ? super T, ? extends U> function;
    private final long size;
    private final int characteristics;
    public ScanLeftSpliterator(Spliterator<T> source, U identity,
            BiFunction<? super U, ? super T, ? extends U> function) {
        super();
        this.source = source;
        this.current = identity;
        this.function = function;
        
        size = source.estimateSize();
        characteristics= source.characteristics() & Spliterator.ORDERED;;
    }
    
    /* (non-Javadoc)
     * @see java.util.Spliterator#forEachRemaining(java.util.function.Consumer)
     */
    @Override
    public void forEachRemaining(Consumer<? super U> action) {
        
        source.forEachRemaining(e->{
            action.accept( current=function.apply(current,e));
            
        });
        
    }

    boolean advance =true;
    @Override
    public boolean tryAdvance(Consumer<? super U> action) {
       
       return source.tryAdvance(e->{
           action.accept(current= function.apply(current, e));
            
          
       });
       
      
    }

    @Override
    public Spliterator<U> trySplit() {
        return this;
    }

    @Override
    public long estimateSize() {
        return  size;
    }

    @Override
    public int characteristics() {
       return characteristics;
    }

   

}
