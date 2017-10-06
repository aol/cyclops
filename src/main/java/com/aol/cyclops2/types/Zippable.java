package com.aol.cyclops2.types;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops2.types.functor.Transformable;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.companion.Monoids;
import cyclops.function.Semigroup;
import cyclops.companion.Semigroups;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.reactivestreams.Publisher;

import cyclops.stream.ReactiveSeq;

/**
 *
 * A Data Type that can be comined with another data type 
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) of this Zippable
 */
public interface Zippable<T> extends Iterable<T>, Transformable<T> {

    /**
     * Combine two applicatives together using the provided BinaryOperator (Semigroup, Monoid and Reducer all
     * extend BinaryOperator - checkout SemigroupK and Monoids for a large number of canned combinations).
     * If this Applicative is a scalar value the provided value is combined with that value,
     * otherwise the value is combined pair wise with all value in a non-scalar datastructure
     *
     * @see Semigroup
     * @see Semigroups
     * @see Monoid
     * @see Monoids
     *
     * To lift any Semigroup (or monoid) up to handling Applicatives use the combineApplicatives operator in SemigroupK
     * {@see com.aol.cyclops2.SemigroupK#combineApplicatives(BiFunction) } or Monoids
     * { {@see com.aol.cyclops2.Monoids#combineApplicatives(java.util.function.Function, com.aol.cyclops2.function.Monoid)
     *  }
     * <pre>
     * {@code
     *
     *
     *  BinaryOperator<Zippable<Integer>> sumMaybes = SemigroupK.combineScalarFunctors(SemigroupK.intSum);
        Maybe.just(1).zip(sumMaybes, Maybe.just(5))
       //Maybe.just(6));
     * }
     * </pre>
     *
     * @param combiner
     * @param app
     * @return
     */
    default  Zippable<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return combiner.apply(this, app);
    }

    default <R> Zippable<R> zipWith(Iterable<Function<? super T,? extends R>> fn){
        return zip(fn,(a,b)->b.apply(a));
    }
    default <R> Zippable<R> zipWithS(Stream<Function<? super T,? extends R>> fn){
        return zipS(fn,(a,b)->b.apply(a));
    }
    default <R> Zippable<R> zipWithP(Publisher<Function<? super T,? extends R>> fn){
        return zipP(fn,(a,b)->b.apply(a));
    }

    /**
     * Zip (combine) this Zippable with the supplied Iterable using the supplied combining function
     * 
     * @param iterable to zip with
     * @param fn Zip function
     * @return Combined zippable
     */

    default <T2, R> Zippable<R> zip(final Iterable<? extends T2> iterable, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return ReactiveSeq.fromIterable(this)
                          .zip(iterable, fn);
    }

    /**
     * Zip (combine) this Zippable with the supplied Publisher, using the supplied combining function
     * 
     * @param publisher to combine with
     * @param fn Zip / combining function
     * @return Combined zippable
     */
    default <T2, R> Zippable<R> zipP(final Publisher<? extends T2> publisher,final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return ReactiveSeq.fromIterable(this)
                          .zipP(publisher,fn);
    }



    /**
     * Zip (combine) this Zippable with the supplied Stream, using the supplied combining function
     * 
     * @param other Stream to combine with
     * @param zipper Zip / combining function
     * @return Combined zippable
     */
    default <U, R> Zippable<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>) ReactiveSeq.fromStream(other), zipper);
    }

    /**
     * Zip (combine) this Zippable with the supplied Stream combining both into a Tuple2
     * 
     * @param other Stream to combine with
     * @return Combined Zippable
     */
    default <U> Zippable<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return zipS(other, (a, b) -> Tuple.tuple(a, b));
    }
    default <U> Zippable<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return zipP(other, (a, b) -> Tuple.tuple(a, b));
    }

    /**
     * Zip (combine) this Zippable with the supplied Iterable combining both into a Tuple2
     * 
     * @param other Iterable to combine with
     * @return
     */
    default <U> Zippable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return zipS((Stream<? extends U>) ReactiveSeq.fromIterable(other));
    }
    /**
     * zip 3 Streams into one
     *
     * <pre>
     * {@code
     *  List<Tuple3<Integer, Integer, Character>> list = of(1, 2, 3, 4, 5, 6).zip3(of(100, 200, 300, 400), of('a', 'b', 'c')).collect(CyclopsCollectors.toList());
     *
     *  // [[1,100,'a'],[2,200,'b'],[3,300,'c']]
     * }
     *
     * </pre>
     */
    default <S, U> Zippable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a._1(),a._2(),b));
    }
    default <S, U,R> Zippable<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third,
                                                  final Fn3<? super T, ? super S, ? super U,? extends R> fn3) {
        return (Zippable<R>)zip3(second,third).map(t-> fn3.apply(t._1(),t._2(),t._3()));
    }

    /**
     * zip 4 Streams into 1
     *
     * <pre>
     * {@code
     *  List<Tuple4<Integer, Integer, Character, String>> list = of(1, 2, 3, 4, 5, 6).zip4(of(100, 200, 300, 400), of('a', 'b', 'c'), of("hello", "world"))
     *          .collect(CyclopsCollectors.toList());
     *
     * }
     * // [[1,100,'a',"hello"],[2,200,'b',"world"]]
     * </pre>
     */
    default <T2, T3, T4> Zippable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                 final Iterable<? extends T4> fourth) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a._1(),a._2(),b))
                                       .zip(fourth,(a,b)->(Tuple4<T,T2,T3,T4>)Tuple.tuple(a._1(),a._2(),a._3(),b));
    }
    default <T2, T3, T4,R> Zippable<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                      final Iterable<? extends T4> fourth,
                                      final Fn4<? super T, ? super T2, ? super T3,? super T4,? extends R> fn) {
        return (Zippable<R>)zip4(second,third,fourth).map(t->fn.apply(t._1(),t._2(),t._3(),t._4()));
    }
}
