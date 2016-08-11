package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An interoperability trait for Monoids
 * 
 * Also inteded for use with Java 8 Streams (reduce method)
 * 
 * Practically the method signature to reduce matches the Monoid interface
 * Monoids could regrarded as immutable equivalents to JDK Collectors for Immutable Reduction
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Monoid<T> extends Semigroup<T> {

    /**
     * An element that when provided as a parameter to the combiner with another value, results
     * in the other value being returned
     * e.g.
     * <pre>
     *  0  + 1  = 1
     *  
     *  0 is zero()
     *  
     *  1 * 2 = 2
     *  
     *   1 is zero()
     *   
     *   "" + "hello" = "hello"
     *   
     *  "" is zero()
     *  </pre>
     * @return
     */
    T zero();

    default T reduce(Stream<T> toReduce) {
        return toReduce.reduce(zero(), reducer());
    }

    public static <T> Monoid<T> of(T zero, Semigroup<T> group) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(T t, T u) {
                return group.apply(t, u);
            }
        };
    }

    public static <T> Monoid<T> of(T zero, Function<T, Function<T, T>> combiner) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(T t, T u) {
                return combiner.apply(t)
                               .apply(u);
            }

        };
    }

    public static <T> Monoid<T> fromBiFunction(T zero, BiFunction<T, T, T> combiner) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(T t, T u) {
                return combiner.apply(t, u);
            }
        };
    }
}
