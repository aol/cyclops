package com.aol.cyclops2.internal.stream.spliterators;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;


public class UnfoldRightSpliterator<T1,T> implements Spliterator<T>, CopyableSpliterator<T> {

    private final T1 in;
    private final Function<? super T1, ? extends Optional<Tuple2<T1,T>>> fn;

    public UnfoldRightSpliterator(T1 in, Function<? super T1, ? extends Optional<Tuple2<T1,T>>> fn) {
        this.in = in;
        this.fn = fn;
        current =  Tuple.tuple(in, null);

    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }


    private Tuple2<T1,T> current;

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {

       return fn.apply(current.v1)
                .map(result->{
                    System.out.println(current.v1);
                    System.out.println(current.v2);
                    System.out.println(result);
                    current = result;
                    action.accept(result.v2);
                    return result;
                }).isPresent();

    }


    @Override
    public Spliterator<T> trySplit() {

        return this;
    }


    @Override
    public Spliterator<T> copy() {
        return new UnfoldRightSpliterator<>(in,fn);
    }
}
