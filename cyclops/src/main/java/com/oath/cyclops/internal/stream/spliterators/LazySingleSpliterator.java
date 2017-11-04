package com.oath.cyclops.internal.stream.spliterators;

import com.oath.cyclops.internal.stream.StreamX;
import cyclops.reactive.ReactiveSeq;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;


public class LazySingleSpliterator<T, X,R> implements Spliterator<R>, CopyableSpliterator<R> {

    private final X in;
    private final Function<? super X,? extends R> fn;

    public LazySingleSpliterator( X in,Function<? super X,? extends R> fn) {
        this.in = in;
        this.fn = fn;
    }

    @Override
    public long estimateSize() {
        return 1l;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }



    private boolean closed = false;
    @Override
    public boolean tryAdvance(final Consumer<? super R> action) {
        if(closed)
            return false;

        action.accept(fn.apply(in));

        return closed =true;

    }

    @Override
    public Spliterator<R> trySplit() {

        return this;
    }


    @Override
    public Spliterator<R> copy() {
        if(in instanceof StreamX){
            return new LazySingleSpliterator<>((X)ReactiveSeq.fromSpliterator(((StreamX)in).spliterator()), fn);
        }else {
            return new LazySingleSpliterator<>(in, fn);
        }
    }
}
