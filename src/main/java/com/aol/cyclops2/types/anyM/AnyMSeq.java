package com.aol.cyclops2.types.anyM;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops2.types.*;


import com.aol.cyclops2.types.foldable.ConvertableSequence;
import com.aol.cyclops2.types.traversable.IterableX;
import com.aol.cyclops2.types.traversable.Traversable;
import cyclops.async.adapters.QueueFactory;
import cyclops.collectionx.mutable.*;
import cyclops.collectionx.immutable.*;
import cyclops.control.anym.WitnessType;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cyclops.function.Monoid;
import cyclops.control.anym.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.control.lazy.Trampoline;
import cyclops.control.Either;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import cyclops.function.Predicates;
import cyclops.function.Function4;
import cyclops.function.Function3;

/**
 * Wrapper around 'Any' non-scalar 'M'onad
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of elements managed by wrapped non-scalar Monad.
 */
public interface AnyMSeq<W extends WitnessType<W>,T> extends AnyM<W,T>, IterableX<T>, Publisher<T> {


    default <R> AnyMSeq<W,R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn){
        return this.flatMap(fn.andThen(i->unitIterator(i.iterator())));
    }
    default <R> AnyMSeq<W,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn){
        return this.flatMap(fn.andThen(i->unitIterator(ReactiveSeq.fromPublisher(i).iterator())));
    }
    default <R> AnyMSeq<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn){
        return this.flatMap(fn.andThen(i->unitIterator(i.iterator())));
    }

    /**
     * Perform a four level nested internal iteration over this monad and the
     * supplied monads
     *
     * 
     * @param monad1
     *            Nested Monad to iterate over
     * @param monad2
     *            Nested Monad to iterate over
     * @param monad3
     *            Nested Monad to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Monad that generates the new elements
     * @return AnyMSeq with elements generated via nested iteration
     */
    default <R1, R2, R3,R> AnyMSeq<W,R> forEach4(final Function<? super T, ? extends AnyM<W,R1>> monad1,
                        final BiFunction<? super T,? super R1, ? extends AnyM<W,R2>> monad2,
                            final Function3<? super T, ? super R1, ? super R2, ? extends AnyM<W,R3>> monad3,
                            final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
       
        return this.flatMapA(in -> {

            AnyM<W,R1> a = monad1.apply(in);
            return a.flatMapA(ina -> {
                AnyM<W,R2> b = monad2.apply(in, ina);
                return b.flatMapA(inb -> {
                    AnyM<W,R3> c = monad3.apply(in, ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }
    /**
     * Perform a four level nested internal iteration over this monad and the
     * supplied monads
     * 

     * 
     * @param monad1
     *            Nested Monad to iterate over
     * @param monad2
     *            Nested Monad to iterate over
     * @param monad3
     *            Nested Monad to iterate over
     * @param filterFunction
     *            Filter to applyHKT over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R3,R> AnyMSeq<W,R> forEach4(final Function<? super T, ? extends AnyM<W,R1>> monad1,
            final BiFunction<? super T,? super R1, ? extends AnyM<W,R2>> monad2,
                    final Function3<? super T, ? super R1, ? super R2, ? extends AnyM<W,R3>> monad3,
                        final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){

        return this.flatMapA(in -> {

            AnyM<W,R1> a = monad1.apply(in);
            return a.flatMapA(ina -> {
                AnyM<W,R2> b = monad2.apply(in, ina);
                return b.flatMapA(inb -> {
                    AnyM<W,R3> c = monad3.apply(in, ina,inb);
                    return c.filter(in2 -> filterFunction.apply(in, ina, inb, in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
        
           
    }
    /**
     * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
     * etc to be injected, for example)
     * 
     * <pre>
     * {@code 
     * AnyM.fromArray(1,2,3)
                        .forEachAnyM2(a->AnyM.fromIntStream(IntStream.range(10,13)),
                                    (a,b)->a+b);
                                    
     * 
     *  //AnyM[11,14,12,15,13,16]
     * }
     * </pre>
     * 
     * 
     * @param monad Nested Monad to iterate over
     * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
     * @return FutureStream with elements generated via nested iteration
     */
    default <R1, R> AnyMSeq<W,R> forEach2(Function<? super T, ? extends AnyM<W,R1>> monad,
            BiFunction<? super T,? super R1, ? extends R> yieldingFunction){  
        return this.flatMapA(in-> { 
            AnyM<W,R1> b = monad.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
      

    }

    /**
     * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
     * etc to be injected, for example)
     * 
     * <pre>
     * {@code 
     * AnyM.fromArray(1,2,3)
                        .forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
                                  (a,b)-> a<3 && b>10,
                                  (a,b)->a+b);
                                    
     * 
     *  //AnyM[14,15]
     * }
     * </pre>
     * @param monad Nested Monad to iterate over
     * @param filterFunction Filter to applyHKT over elements before passing non-filtered values to the yielding function
     * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
     * @return
     */
   default <R1, R> AnyMSeq<W,R> forEach2(Function<? super T, ? extends AnyM<W,R1>> monad, 
            BiFunction<? super T,? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){

       return this.flatMapA(in-> { 
           
           
           AnyM<W,R1> b = monad.apply(in);
           return b.filter(in2-> filterFunction.apply(in,in2))
                   .map(in2->yieldingFunction.apply(in, in2));
       });
        
        
    }

    /** 
     * Perform a three level nested internal iteration over this Stream and the supplied streams
      *<pre>
     * {@code 
     * AnyM.fromArray(1,2)
                        .forEach3(a->AnyM.fromIntStream(IntStream.range(10,13)),
                                 (a,b)->AnyM.fromArray(""+(a+b),"hello world"),
                                 (a,b,c)->AnyM.fromArray(""+(a+b),"hello world"),
                                 (a,b,c,d)->c+":"a+":"+b);
                                    
     * 
     *  
     * }
     * </pre> 
     * @param monad1 Nested monad to flatMap over
     * @param monad2 Nested monad to flatMap over
     * @param filterFunction Filter to applyHKT over elements before passing non-filtered values to the yielding function
     * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
     * @return AnyM with elements generated via nested iteration
     */
    default <R1, R2, R> AnyMSeq<W,R> forEach3(Function<? super T, ? extends AnyM<W,R1>> monad1,
            BiFunction<? super T, ? super R1, ? extends AnyM<W,R2>> monad2,
            Function3<? super T,? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){

        return this.flatMapA(in -> {

            AnyM<W,R1> a = monad1.apply(in);
            return a.flatMapA(ina -> {
                AnyM<W,R2> b = monad2.apply(in, ina);
                return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });

        });
        
        
    }
    
    /**
     * Perform a three level nested internal iteration over this AnyM and the supplied monads
     *<pre>
     * {@code 
     * AnyM.fromArray(1,2,3)
                    .forEach3(a->AnyM.fromStream(IntStream.range(10,13)),
                             (a,b)->AnyM.fromArray(""+(a+b),"hello world"),
                             (a,b,c)-> c!=3,
                             (a,b,c)->c+":"a+":"+b);
                                
     * 
     *  //AnyM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
    * </pre> 
     * 
     * @param monad1 Nested Stream to iterate over
     * @param monad2 Nested Stream to iterate over
     * @param yieldingFunction Function with pointers to the current element from both Monads that generates the new elements
     * @return AnyM with elements generated via nested iteration
     */
    default <R1, R2, R> AnyMSeq<W,R> forEach3(Function<? super T, ? extends AnyM<W,R1>> monad1,
            BiFunction<? super T, ? super R1, ? extends AnyM<W,R2>> monad2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
        return this.flatMapA(in -> {

            AnyM<W,R1> a = monad1.apply(in);
            return a.flatMapA(ina -> {
                AnyM<W,R2> b = monad2.apply(in, ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });

        });
    
    }
    /**
     * Equivalence test, returns true if this Monad is equivalent to the supplied monad
     * e.g.
     * <pre>
     * {code
     *    Stream.of(1) and Arrays.asList(1) are equivalent
     * }
     * </pre>
     * 
     * 
     * @param t Monad to compare to
     * @return true if equivalent
     */
    default boolean eqv(final AnyMSeq<?,T> t) {
        return Predicates.eqvIterable(t)
                         .test(this);
    }

    /* (non-Javadoc)
     * @see cyclops2.monads.AnyM#collect(java.util.reactiveStream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream().collect(collector);

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#traversable()
     */
    @Override
    default Traversable<T> traversable() {
        final Object o = unwrap();
        if (o instanceof Traversable) {
            return (Traversable) o;
        }
        return stream();
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#limit(long)
     */
    @Override
    default AnyMSeq<W,T> limit(final long num) {

        return fromIterable(IterableX.super.limit(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> limitWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> limitUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#limitLast(int)
     */
    @Override
    default AnyMSeq<W,T> limitLast(final int num) {

        return fromIterable(IterableX.super.limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default AnyMSeq<W,T> onEmpty(final T value) {
        return fromIterable(IterableX.super.onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default AnyMSeq<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return fromIterable(IterableX.super.onEmptyGet(supplier));
    }
    FunctionalAdapter<W> adapter();
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> AnyMSeq<W,T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return fromIterable(IterableX.super.onEmptyThrow(supplier));
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        if (unwrap() instanceof Publisher) {
            ((Publisher) unwrap()).subscribe(sub);
        } else {
            this.stream()
                .subscribe(sub);
        }
    }

    


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMSeq<W,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMSeq<W,R>) AnyM.super.trampoline(mapper);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#cycle(int)
     */
    @Override
    default AnyMSeq<W,T> cycle(final long times) {

        return fromIterable(IterableX.super.cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#cycle(cyclops2.function.Monoid, int)
     */
    @Override
    default AnyMSeq<W,T> cycle(final Monoid<T> m, final long times) {

        return fromIterable(IterableX.super.cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> cycleWhile(final Predicate<? super T> predicate) {
        
        return fromIterable(IterableX.super.cycleWhile(predicate));
    }
    @Override
    default Either<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Either.right(this);
    }
    @Override
    default <T> AnyMSeq<W,T> fromIterable(Iterable<T> t){

        if(t instanceof AnyMSeq) {
            AnyMSeq check =(AnyMSeq) t;
            if(check.adapter() == this.adapter())
                return check;
        }
        return  (AnyMSeq<W,T>)adapter().unitIterable(t);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return fromIterable(IterableX.super.cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return fromIterable(IterableX.super.zip(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<W,R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return fromIterable(IterableX.super.zipS(other, zipper));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return fromIterable(IterableX.super.zipS(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip(java.lang.Iterable)
     */
    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return fromIterable(IterableX.super.zip(other));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> AnyMSeq<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return fromIterable(IterableX.super.zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> AnyMSeq<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return fromIterable(IterableX.super.zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zipWithIndex()
     */
    @Override
    default AnyMSeq<W,Tuple2<T, Long>> zipWithIndex() {

        return fromIterable(IterableX.super.zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#sliding(int)
     */
    @Override
    default AnyMSeq<W,VectorX<T>> sliding(final int windowSize) {

        return fromIterable(IterableX.super.sliding(windowSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#sliding(int, int)
     */
    @Override
    default AnyMSeq<W,VectorX<T>> sliding(final int windowSize, final int increment) {

        return fromIterable(IterableX.super.sliding(windowSize, increment));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<W,C> grouped(final int size, final Supplier<C> supplier) {

        return fromIterable(IterableX.super.grouped(size, supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return fromIterable(IterableX.super.groupedUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default AnyMSeq<W,ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return fromIterable(IterableX.super.groupedStatefullyUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default AnyMSeq<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return fromIterable(IterableX.super.combine(predicate, op));
    }
    @Override
    default AnyMSeq<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return fromIterable(IterableX.super.combine(op,predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return fromIterable(IterableX.super.groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromIterable(IterableX.super.groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromIterable(IterableX.super.groupedUntil(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#grouped(int)
     */
    @Override
    default AnyMSeq<W,ListX<T>> grouped(final int groupSize) {

        return fromIterable(IterableX.super.grouped(groupSize));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> takeWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.takeWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> dropWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.dropWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> takeUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.takeUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> dropUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.dropUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropRight(int)
     */
    @Override
    default AnyMSeq<W,T> dropRight(final int num) {

        return fromIterable(IterableX.super.dropRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeRight(int)
     */
    @Override
    default AnyMSeq<W,T> takeRight(final int num) {

        return fromIterable(IterableX.super.takeRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#reverse()
     */
    @Override
    default AnyMSeq<W,T> reverse() {

        return fromIterable(IterableX.super.reverse());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#shuffle()
     */
    @Override
    default AnyMSeq<W,T> shuffle() {

        return fromIterable(IterableX.super.shuffle());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#shuffle(java.util.Random)
     */
    @Override
    default AnyMSeq<W,T> shuffle(final Random random) {

        return fromIterable(IterableX.super.shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#distinct()
     */
    @Override
    default AnyMSeq<W,T> distinct() {

        return fromIterable(IterableX.super.distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default AnyMSeq<W,T> scanLeft(final Monoid<T> monoid) {

        return fromIterable(IterableX.super.scanLeft(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> AnyMSeq<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return fromIterable(IterableX.super.scanLeft(seed, function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default AnyMSeq<W,T> scanRight(final Monoid<T> monoid) {

        return fromIterable(IterableX.super.scanRight(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> AnyMSeq<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return fromIterable(IterableX.super.scanRight(identity, combiner));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#sorted()
     */
    @Override
    default AnyMSeq<W,T> sorted() {

        return fromIterable(IterableX.super.sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default AnyMSeq<W,T> sorted(final Comparator<? super T> c) {

        return fromIterable(IterableX.super.sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#skip(long)
     */
    @Override
    default AnyMSeq<W,T> skip(final long num) {

        return fromIterable(IterableX.super.skip(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> skipWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> skipUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default AnyMSeq<W,T> intersperse(final T value) {

        return fromIterable(IterableX.super.intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#skipLast(int)
     */
    @Override
    default AnyMSeq<W,T> skipLast(final int num) {

        return fromIterable(IterableX.super.skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#slice(long, long)
     */
    @Override
    default AnyMSeq<W,T> slice(final long from, final long to) {

        return fromIterable(IterableX.super.slice(from, to));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> AnyMSeq<W,T> sorted(final Function<? super T, ? extends U> function) {

        return fromIterable(IterableX.super.sorted(function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.ExtendedTraversable#permutations()
     */
    @Override
    default AnyMSeq<W,ReactiveSeq<T>> permutations() {

        return fromIterable(IterableX.super.permutations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.ExtendedTraversable#combinations(int)
     */
    @Override
    default AnyMSeq<W,ReactiveSeq<T>> combinations(final int size) {

        return fromIterable(IterableX.super.combinations(size));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.ExtendedTraversable#combinations()
     */
    @Override
    default AnyMSeq<W,ReactiveSeq<T>> combinations() {

        return fromIterable(IterableX.super.combinations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMSeq<W,U> ofType(final Class<? extends U> type) {

        return (AnyMSeq<W,U>) AnyM.super.ofType(type);
    }

    @Override
    default AnyMSeq<W, T> notNull() {
        return (AnyMSeq<W,T>) AnyM.super.notNull();
    }

    /* (non-Javadoc)
         * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
         */
    @Override
    default AnyMSeq<W,T> filterNot(final Predicate<? super T> fn) {
        return (AnyMSeq<W,T>) AnyM.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <U> AnyMSeq<W,U> unitIterator(Iterator<U> U){
        return (AnyMSeq<W,U>)adapter().unitIterable(()->U);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.factory.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMSeq<W,T> emptyUnit(){
        return (AnyMSeq<W,T> )empty();
    }

    


    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> filter(Predicate<? super T> p){
        return (AnyMSeq<W,T>)AnyM.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#transform(java.util.function.Function)
     */
    @Override
    default <R> AnyMSeq<W,R> map(Function<? super T, ? extends R> fn){
        return (AnyMSeq<W,R>)AnyM.super.map(fn);
    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.reactiveBuffer.ReactiveStreamsTerminalOperations#forEach(long, java.util.function.Consumer)
     */
    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        return this.stream()
                   .forEach(numberOfElements, consumer);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.reactiveBuffer.ReactiveStreamsTerminalOperations#forEach(long, java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                       final Consumer<? super Throwable> consumerError) {
        return this.stream()
                   .forEach(numberOfElements, consumer, consumerError);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.reactiveBuffer.ReactiveStreamsTerminalOperations#forEach(long, java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
     */
    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                       final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return this.stream()
                   .forEach(numberOfElements, consumer, consumerError, onComplete);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.reactiveBuffer.ReactiveStreamsTerminalOperations#forEach(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        this.stream()
            .forEach(consumerElement, consumerError);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.reactiveBuffer.ReactiveStreamsTerminalOperations#forEach(java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
     */
    @Override
    default <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                               final Runnable onComplete) {
        this.stream()
            .forEach(consumerElement, consumerError, onComplete);

    }


    /* (non-Javadoc)
         * @see com.aol.cyclops2.monad.AnyM#peek(java.util.function.Consumer)
         */
    @Override
    default AnyMSeq<W,T> peek(final Consumer<? super T> c) {
        return map(i -> {
            c.accept(i);
            return i;
        });
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#flatten()
     */
    
    static <W extends WitnessType<W>,T1> AnyMSeq<W,T1> flatten(AnyMSeq<W,AnyMSeq<W,T1>> nested){
        return nested.flatMap(Function.identity());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#aggregate(com.aol.cyclops2.monad.AnyM)
     */
    @Override
    default AnyMSeq<W,List<T>> aggregate(AnyM<W,T> next){
        return (AnyMSeq<W,List<T>>)AnyM.super.aggregate(next);
    }
    
  

    @Override
    default <R> AnyMSeq<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return (AnyMSeq<W,R>)AnyM.super.flatMapA(fn);
    }

    @Override
    default <R> AnyMSeq<W,R> retry(final Function<? super T, ? extends R> fn) {
        return (AnyMSeq<W,R>)AnyM.super.retry(fn);
    }

    @Override
    default <R> AnyMSeq<W,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyMSeq<W,R>)AnyM.super.retry(fn,retries,delay,timeUnit);
    }
    @Override
    default AnyMSeq<W,T> prependS(Stream<? extends T> stream) {
        return fromIterable(IterableX.super.prependS(stream));
    }

    @Override
    default AnyMSeq<W,T> append(T... values) {
        return fromIterable(IterableX.super.append(values));
    }

    @Override
    default AnyMSeq<W,T> append(T value) {
        return fromIterable(IterableX.super.append(value));
    }

    @Override
    default AnyMSeq<W,T> prepend(T value) {
        return fromIterable(IterableX.super.prepend(value));
    }

    @Override
    default AnyMSeq<W,T> prepend(T... values) {
        return fromIterable(IterableX.super.prepend(values));
    }

    @Override
    default AnyMSeq<W,T> insertAt(int pos, T... values) {
        return fromIterable(IterableX.super.insertAt(pos,values));
    }

    @Override
    default AnyMSeq<W,T> deleteBetween(int start, int end) {
        return fromIterable(IterableX.super.deleteBetween(start,end));
    }

    @Override
    default AnyMSeq<W,T> insertAtS(int pos, Stream<T> stream) {
        return fromIterable(IterableX.super.insertAtS(pos,stream));
    }

    @Override
    default AnyMSeq<W,T> recover(final Function<? super Throwable, ? extends T> fn) {
        return fromIterable(IterableX.super.recover(fn));
    }

    @Override
    default <EX extends Throwable> AnyMSeq<W,T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return fromIterable(IterableX.super.recover(exceptionClass,fn));
    }




    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMSeq<W,T> unit(T value){
        return (AnyMSeq<W,T>)AnyM.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#zero()
     */
    @Override
    default <T> AnyMSeq<W,T> empty(){
        return (AnyMSeq<W,T>)AnyM.super.empty();
    }

    default <R> AnyMSeq<W,R> flatMap(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return flatMapA(fn);
    }
   
    @Override
    ReactiveSeq<T> stream();

    @Override
    default ConvertableSequence<T> to() {
        return new ConvertableSequence<>(traversable());
    }

    default AnyMSeq<W,T> mergeP(final QueueFactory<T> factory, final Publisher<T>... publishers) {
    	ReactiveSeq<T> reactiveSeq = stream().mergeP(factory, publishers);
    	return (AnyMSeq<W,T>) reactiveSeq.anyM();
    }
    
    default AnyMSeq<W,T> mergeP(final Publisher<T>... publishers) {
    	return (AnyMSeq<W,T>) stream().mergeP(publishers).anyM();
    }

    @Override
    default AnyMSeq<W,T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return fromIterable(IterableX.super.zip(combiner,app));
    }

    @Override
    default <R> AnyMSeq<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return fromIterable(IterableX.super.zipWith(fn));
    }

    @Override
    default <R> AnyMSeq<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return fromIterable(IterableX.super.zipWithS(fn));
    }

    @Override
    default <R> AnyMSeq<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return fromIterable(IterableX.super.zipWithP(fn));
    }

    @Override
    default <T2, R> AnyMSeq<W,R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return fromIterable(IterableX.super.zipP(publisher,fn));
    }



    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return fromIterable(IterableX.super.zipP(other));
    }


    @Override
    default <S, U, R> AnyMSeq<W,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return fromIterable(IterableX.super.zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> AnyMSeq<W,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return fromIterable(IterableX.super.zip4(second,third,fourth,fn));
    }



}