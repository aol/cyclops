package com.oath.cyclops.internal.stream.spliterators;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

public class LimitLastSpliterator<T> extends AbstractSpliterator<T> implements CopyableSpliterator<T>{

    public static <T> Spliterator<T> limitLast(Spliterator<T> source, int limit){

        if(limit==0){
            return Spliterators.emptySpliterator();
        }
        if(limit==source.getExactSizeIfKnown()){ //right sized already
            return source;
        }
        if(limit==1)
            return new LimitLastOneSpliterator<T>(source);
        return new LimitLastSpliterator<T>(source,limit);
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
         if(!requestedAll) {
             source.forEachRemaining(e -> {
                 if (buffer.size() == limit) {
                     buffer.poll();
                 }
                 buffer.offer(e);
             });
         }
            requestedAll=true;

            if(buffer.size()>0){
                action.accept(buffer.pop());
            }
            //need to handle case where external subscription is not closed
            return buffer.size()>0;
     }


    @Override
    public Spliterator<T> copy() {
        return new LimitLastSpliterator<T>(copy(source),limit);
    }
}
