package com.oath.cyclops.internal.stream.spliterators;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;


public class OnErrorBreakWithPublisherSpliterator<T, X extends Throwable> implements CopyableSpliterator<Publisher<? extends T>>{
    private final Spliterator<T> source;
    private final  Function<? super Throwable,? extends Publisher<? extends T>>  fn;


    public OnErrorBreakWithPublisherSpliterator(Spliterator<T> source, Function<? super Throwable,? extends Publisher<? extends T>> fn) {
        this.source = source;
        this.fn = fn;

    }



    boolean closed =false;
    @Override
    public boolean tryAdvance(Consumer<? super Publisher<? extends T>> action) {

        if(closed)
            return false;
         try {
             return source.tryAdvance(in->action.accept(Spouts.of(in)));
         }catch(Throwable t){
                ReactiveSeq<T> rs = Spouts.from(fn.apply(t));
                 action.accept(rs);
                 closed = true;
                 return false;


         }

    }



    @Override
    public Spliterator<Publisher<? extends T>> copy() {
        return new OnErrorBreakWithPublisherSpliterator(CopyableSpliterator.copy(source),fn);
    }

    @Override
    public Spliterator<Publisher<? extends T>> trySplit() {
        return this;
    }


    @Override
    public long estimateSize() {
        return source.estimateSize();
    }


    @Override
    public int characteristics() {
        return source.characteristics() & ~(SORTED | DISTINCT);
    }
}
