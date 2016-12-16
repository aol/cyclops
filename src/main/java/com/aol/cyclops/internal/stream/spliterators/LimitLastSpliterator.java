package com.aol.cyclops.internal.stream.spliterators;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class LimitLastSpliterator<T> extends AbstractSpliterator<T>{

    public static <T> Stream<T> limitLast(Stream<T> stream, int limit){
        Spliterator<T> source = stream.spliterator();
        if(limit==0){
            return Stream.of();
        }
        if(limit==1)
            return StreamSupport.stream(new LimitLastOneSpliterator<T>(source), false);
        return StreamSupport.stream(new LimitLastSpliterator<T>(source,limit), false);
    }
  
    
    private final ArrayDeque<T> buffer;
    private final int limit;
    private final Spliterator<T> source;

    public LimitLastSpliterator(final Spliterator<T> source, final int limit) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);
        buffer = new ArrayDeque<>(
                                  limit);
        this.source = source;;
        this.limit = limit;
    }

    
    
     boolean requestedAll =false;
     @Override
     public boolean tryAdvance(Consumer<? super T> action) {
           source.forEachRemaining(e->{
                    if (buffer.size() == limit) {
                        buffer.poll();
                    }
                    
                    buffer.offer(e);
               });
         
         
            if(buffer.size()>0){
                action.accept(buffer.pop());
            }
            //need to handle case where external subscription is not closed
            return buffer.size()>0;
     }
        
    
    
   
}
