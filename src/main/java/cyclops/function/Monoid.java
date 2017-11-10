package cyclops.function;

import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Semigroups;
import cyclops.data.ImmutableList;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.Cokleisli;
import cyclops.typeclasses.Kleisli;
import cyclops.typeclasses.functions.MonoidK;
import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An interface that represents a Monoid {@link https://en.wikipedia.org/wiki/Monoid#Monoids_in_computer_science}
 *
 * Also inteded for use with Java 8 Streams (reduce method)
 *
 * Practically the method signature to reduce matches the Monoid interface
 * Monoids could regrarded as immutable equivalents to JDK CyclopsCollectors for Immutable Reduction
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

    default <R> R visit(BiFunction<? super Semigroup<T>,? super T,? extends R> visitFn){
        return visitFn.apply(this,zero());
    }

    /**
     * Perform a reduction operation on the supplied Stream
     *
     * @param toReduce Stream to reduce
     * @return Reduced value
     */
    default T foldLeft(final Stream<T> toReduce) {
        return toReduce.reduce(zero(), this);
    }
    default  ReactiveSeq<T> foldLeftAsync(final Publisher<T> toFold){
        return Spouts.from(toFold).reduceAll(this.zero(),this);
    }
    default  T foldLeft(final Iterable<T> toFold){
        return ReactiveSeq.fromIterable(toFold).foldLeft(this);
    }
    default  T foldLeft(final ReactiveSeq<T> toFold){
        return toFold.foldLeft(this);
    }


    default  T fold(final T toFold){
        return foldLeft(Arrays.asList(zero(),toFold));
    }


    default  T foldRight(final ImmutableList<T> toFold){
        return toFold.foldRight(this);
    }
    default  T foldRight(final ReactiveSeq<T> toFold){
        return toFold.foldRight(this);
    }
    default  T foldRight(final Iterable<T> toFold){
        return ReactiveSeq.fromIterable(toFold).foldRight(this);
    }
    default ReactiveSeq<T> foldRightAsync(final Publisher<T> toReduce) {
        return Spouts.from(toReduce).reduceAll(zero(), this);
    }

    default <A> T foldMap(final Publisher<A> toFoldMap, Function<? super A, ? extends T> mapFn){
        ReactiveSeq<T> toReduce = Spouts.from(toFoldMap).map(mapFn);
        return toReduce.reduce(zero(),this);
    }
    default <A> T foldMap(final Stream<A> toFoldMap, Function<? super A, ? extends T> mapFn){
        Stream<T> toReduce = toFoldMap.map(mapFn);
        return toReduce.reduce(zero(),this);
    }
    default <A> T foldMap(final Iterable<A> toFoldMap, Function<? super A, ? extends T> mapFn){
        ReactiveSeq<T> toReduce = ReactiveSeq.fromIterable(toFoldMap).map(mapFn);
        return toReduce.reduce(zero(),this);
    }
    default <A> T foldMap(final ReactiveSeq<A> toFoldMap, Function<? super A, ? extends T> mapFn){
        ReactiveSeq<T> toReduce = toFoldMap.map(mapFn);
        return toReduce.reduce(zero(),this);
    }


    /**
     * Construct a Monoid from the supplied identity element and Semigroup (combiner)
     * @see Semigroups
     *
     * @param zero Identity element  (@see {@link Monoid#zero()}
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
     * @param zero Identity element  (@see {@link Monoid#zero()}
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
     * @param zero Identity element  (@see {@link Monoid#zero()}
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
