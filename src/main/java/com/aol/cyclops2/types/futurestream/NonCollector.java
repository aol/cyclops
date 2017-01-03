package com.aol.cyclops2.types.futurestream;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collector that does nothing
 * 
 * @author johnmcclean
 * 
 * @param <T> the type of input elements to the reduction operation
 * @param <A> the mutable accumulation type of the reduction operation (often
 *            hidden as an implementation detail)
 * @param <R> the result type of the reduction operation
 */
class NonCollector<T, A, R> implements Collector<T, A, R> {

    @Override
    public Supplier<A> supplier() {

        return () -> null;
    }

    @Override
    public BiConsumer<A, T> accumulator() {

        return null;
    }

    @Override
    public BinaryOperator<A> combiner() {

        return null;
    }

    @Override
    public Function<A, R> finisher() {

        return null;
    }

    @Override
    public Set<java.util.stream.Collector.Characteristics> characteristics() {
        return null;
    }

}