package com.oath.cyclops.types;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import com.oath.cyclops.types.functor.Transformable;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.companion.Monoids;
import cyclops.function.Semigroup;
import cyclops.companion.Semigroups;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;

import cyclops.reactive.ReactiveSeq;

/**
 *
 * A Data Type that can be comined with another data type
 *
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) of this Zippable
 */
public interface Zippable<T> extends Iterable<T>, Publisher<T>, Transformable<T> {




    /**
     * Zip (combine) this Zippable with the supplied Iterable using the supplied combining function
     *
     * @param iterable to zip with
     * @param fn Zip function
     * @return Combined zippable
     */

    <T2, R> Zippable<R> zip(final Iterable<? extends T2> iterable, final BiFunction<? super T, ? super T2, ? extends R> fn);


    /**
     * Zip (combine) this Zippable with the supplied Publisher, using the supplied combining function
     *
     * @param fn Zip / combining function
     * @param publisher to combine with
     * @return Combined zippable
     */
    <T2, R> Zippable<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher);

    default <U> Zippable<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return zip((a, b) -> Tuple.tuple(a, b), other);
    }


    default <U> Zippable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return zip(other,Tuple::tuple);
    }

    default <S, U> Zippable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a._1(),a._2(),b));
    }

    default <S, U,R> Zippable<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third,
                                                  final Function3<? super T, ? super S, ? super U,? extends R> fn3) {
        return (Zippable<R>)zip3(second,third).map(t-> fn3.apply(t._1(),t._2(),t._3()));
    }

    default <T2, T3, T4> Zippable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                 final Iterable<? extends T4> fourth) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a._1(),a._2(),b))
                                       .zip(fourth,(a,b)->(Tuple4<T,T2,T3,T4>)Tuple.tuple(a._1(),a._2(),a._3(),b));
    }

    default <T2, T3, T4,R> Zippable<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                      final Iterable<? extends T4> fourth,
                                      final Function4<? super T, ? super T2, ? super T3,? super T4,? extends R> fn) {
        return (Zippable<R>)zip4(second,third,fourth).map(t->fn.apply(t._1(),t._2(),t._3(),t._4()));
    }
}
