package com.oath.cyclops.internal.stream.spliterators;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

public class SkipLastSpliterator<T> extends AbstractSpliterator<T> implements CopyableSpliterator<T>{

    public static <T> Spliterator<T> dropRight(Spliterator<T> source, int skip){

        if(skip==0){
            return source;
        }
        if(skip==source.getExactSizeIfKnown()){ //right sized already
            return Spliterators.emptySpliterator();
        }

        return new SkipLastSpliterator<T>(source,skip);
    }


    private final Deque<T> buffer;
    private final int skip;
    private final Spliterator<T> source;

    public SkipLastSpliterator(final Spliterator<T> source, final int skip) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);
        buffer = skip <1_000 ? new ArrayDeque<>(
                skip) : new LinkedList<>();
        this.source = source;;
        this.skip = skip;
    }


    boolean closed = false;
    boolean canAdvance = true;
    boolean sent = false;

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        source.forEachRemaining(e->{

            if (buffer.size() == skip) {
                action.accept(buffer.poll());

            }
            buffer.offer(e);
        });
        closed = true;
    }

    @Override
     public boolean tryAdvance(Consumer<? super T> action) {
         if(closed)
             return false;
         sent = false;
         while(canAdvance && !sent) {

             canAdvance = source.tryAdvance(e -> {

                 if (buffer.size() == skip) {
                     action.accept(buffer.poll());
                     sent = true;
                 }
                 buffer.offer(e);

             });

         }


         return closed = !(canAdvance && buffer.size()<=skip);


     }


    @Override
    public Spliterator<T> copy() {
        return new SkipLastSpliterator<T>(CopyableSpliterator.copy(source), skip);
    }
}
