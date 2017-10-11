package com.aol.cyclops2.util.stream.pushable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import cyclops.data.tuple.Tuple2;

import cyclops.async.adapters.Adapter;

public abstract class AbstractPushableStream<T, X extends Adapter<T>, R extends Stream<T>> extends Tuple2<X, R> {

    public AbstractPushableStream(final X v1, final R v2) {
        super(v1, v2);
    }

    public X getInput() {
        return _1();
    }

    public R getStream() {
        return _2();
    }

    public <U> U visit(final BiFunction<? super X, ? super R, ? extends U> visitor) {
        return visitor.apply(_1(), _2());
    }

    public void peekStream(final Consumer<? super R> consumer) {
        consumer.accept(_2());
    }

    public void peekInput(final Consumer<? super X> consumer) {
        consumer.accept(_1());
    }

    private static final long serialVersionUID = 1L;

}