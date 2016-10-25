package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An interface that represents a Monoid {@link https://en.wikipedia.org/wiki/Monoid#Monoids_in_computer_science}
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
     * 
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
     * @return Identity element
     */
    T zero();

    /**
     * Perform a reduction operation on the supplied Stream
     * 
     * @param toReduce Stream to reduce
     * @return Reduced value
     */
    default T reduce(final Stream<T> toReduce) {
        return toReduce.reduce(zero(), reducer());
    }

    /**
     * Construct a Monoid from the supplied identity element and Semigroup (combiner)
     * @see com.aol.cyclops.Semigroups
     * 
     * @param zero Identity element  (@see {@link com.aol.cyclops.Monoid#zero()}
     * @param group Combining function or Semigroup
     * @return Monoid consisting of the supplied identity element and combiner
     */
    public static <T> Monoid<T> of(final T zero, final Semigroup<T> group) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(final T t, final T u) {
                return group.apply(t, u);
            }
        };
    }

    /**
     * Construct a Monoid from the supplied identity element and combining function
     * 
     * @param zero Identity element  (@see {@link com.aol.cyclops.Monoid#zero()}
     * @param combiner Combining function
     * @return  Monoid consisting of the supplied identity element and combiner
     */
    public static <T> Monoid<T> of(final T zero, final Function<T, Function<T, T>> combiner) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(final T t, final T u) {
                return combiner.apply(t)
                               .apply(u);
            }

        };
    }

    /**
     * Construct a Monoid from the supplied identity element and combining function
     * 
     * @param zero Identity element  (@see {@link com.aol.cyclops.Monoid#zero()}
     * @param combiner Combining function
     * @return  Monoid consisting of the supplied identity element and combiner
     */
    public static <T> Monoid<T> fromBiFunction(final T zero, final BiFunction<T, T, T> combiner) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return zero;
            }

            @Override
            public T apply(final T t, final T u) {
                return combiner.apply(t, u);
            }
        };
    }
}
