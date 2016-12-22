package com.aol.cyclops.util.stream.pushable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;

import cyclops.async.Adapter;

public abstract class AbstractPushableStream<T, X extends Adapter<T>, R extends Stream<T>> extends Tuple2<X, R> {

    public AbstractPushableStream(final X v1, final R v2) {
        super(v1, v2);
    }

    public X getInput() {
        return v1;
    }

    public R getStream() {
        return v2;
    }

    public <U> U visit(final BiFunction<? super X, ? super R, ? extends U> visitor) {
        return visitor.apply(v1, v2);
    }

    public void peekStream(final Consumer<? super R> consumer) {
        consumer.accept(v2);
    }

    public void peekInput(final Consumer<? super X> consumer) {
        consumer.accept(v1);
    }

    private static final long serialVersionUID = 1L;

}