package com.aol.cyclops.types;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.ReactiveSeq;

/**
 * A Data Type that can be comined with another data type 
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) of this Zippable
 */
public interface Zippable<T> extends Iterable<T>{
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

}
