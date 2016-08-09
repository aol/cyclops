package com.aol.cyclops.types;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.ReactiveSeq;

public interface Zippable<T> extends Iterable<T> {
    /**
     * Zip (combine) this Zippable with the supplied Iterable using the supplied combining function
     * 
     * @param iterable to zip with
     * @param fn Zip function
     * @return Combined zippable
     */
   
    default <T2,R> Zippable<R> zip(Iterable<? extends T2>iterable,BiFunction<? super T,? super T2,? extends R> fn){
        return ReactiveSeq.fromIterable(this).zip(iterable,fn);
    }
    
    /**
     * Zip (combine) this Zippable with the supplied Publisher, using the supplied combining function
     * 
     * @param publisher to combine with
     * @param fn Zip / combining function
     * @return Combined zippable
     */
    default <T2,R> Zippable<R> zip(BiFunction<? super T,? super T2,? extends R> fn,Publisher<? extends T2> publisher){
        return ReactiveSeq.fromIterable(this).zip(fn,publisher);
    }
    
    /**
     * Zip (combine) this Zippable with the supplied Seq, using the supplied combining function
     * 
     * @param other Seq to combine with
     * @param zipper Zip / combining function
     * @return Combined zippable
     */
    default <U, R> Zippable<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>)other,zipper);
    }
    /**
     * Zip (combine) this Zippable with the supplied Stream, using the supplied combining function
     * 
     * @param other Stream to combine with
     * @param zipper Zip / combining function
     * @return Combined zippable
     */
    default <U, R> Zippable<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>)ReactiveSeq.fromStream(other),zipper);
    }
    
    /**
     * Zip (combine) this Zippable with the supplied Stream combining both into a Tuple2
     * 
     * @param other Stream to combine with
     * @return Combined Zippable
     */
    default <U> Zippable<Tuple2<T, U>> zip(Stream<? extends U> other){
        return zip(other,(a,b)->Tuple.tuple(a,b));
    }
    
    /**
     * Zip (combine) this Zippable with the supplied Seq combining both into a Tuple2
     * 
     * @param other Seq to combine with
     * @return Combined Zippable
     */
    default <U> Zippable<Tuple2<T, U>> zip(Seq<? extends U> other){
        return zip((Stream<? extends U> )other);
    }
    
    /**
     * Zip (combine) this Zippable with the supplied Iterable combining both into a Tuple2
     * 
     * @param other Iterable to combine with
     * @return
     */
    default <U> Zippable<Tuple2<T, U>> zip(Iterable<? extends U> other){
        return zip((Stream<? extends U> )ReactiveSeq.fromIterable(other));
    }

    
    
}
