package com.oath.cyclops.anym;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;


import com.oath.cyclops.anym.transformers.TransformerTraversable;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.RecoverableTraversable;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.monads.WitnessType;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.control.Either;
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
public interface AnyMSeq<W extends WitnessType<W>,T> extends AnyM<W,T>, TransformerTraversable<T>,IterableX<T>,  RecoverableTraversable<T>, Publisher<T> {


    default <R> AnyMSeq<W,R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn){
        return this.flatMap(fn.andThen(i-> unitIterable(i)));
    }
    default <R> AnyMSeq<W,R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn){
        return this.flatMap(fn.andThen(i-> unitIterable(ReactiveSeq.fromPublisher(i))));
    }
    default <R> AnyMSeq<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn){
        return this.flatMap(fn.andThen(i-> unitIterable(ReactiveSeq.fromStream(i))));
    }

    @Override
    default boolean isEmpty() {
        return IterableX.super.isEmpty();
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


    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream().collect(collector);

    }



    @Override
    default Traversable<T> traversable() {
        final Object o = unwrap();
        if (o instanceof Traversable) {
            return (Traversable) o;
        }
        return stream();
    }




    @Override
    default AnyMSeq<W,T> limit(final long num) {

        return fromIterable(IterableX.super.limit(num));
    }


    @Override
    default AnyMSeq<W,T> limitWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.limitWhile(p));
    }


    @Override
    default AnyMSeq<W,T> limitUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.limitUntil(p));
    }


    @Override
    default AnyMSeq<W,T> limitLast(final int num) {

        return fromIterable(IterableX.super.limitLast(num));
    }


    @Override
    default AnyMSeq<W,T> onEmpty(final T value) {
        return fromIterable(IterableX.super.onEmpty(value));
    }


    @Override
    default AnyMSeq<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return fromIterable(IterableX.super.onEmptyGet(supplier));
    }

    MonadAdapter<W> adapter();


    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        if (unwrap() instanceof Publisher) {
            ((Publisher) unwrap()).subscribe(sub);
        } else {
            this.stream()
                .subscribe(sub);
        }
    }



    @Override
    default AnyMSeq<W,T> cycle(final long times) {

        return fromIterable(IterableX.super.cycle(times));
    }


    @Override
    default AnyMSeq<W,T> cycle(final Monoid<T> m, final long times) {

        return fromIterable(IterableX.super.cycle(m, times));
    }


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


    @Override
    default AnyMSeq<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return fromIterable(IterableX.super.cycleUntil(predicate));
    }


    @Override
    default <U, R> AnyMSeq<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return fromIterable(IterableX.super.zip(other, zipper));
    }

    default <U, R> AnyMSeq<W,R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return fromIterable(IterableX.super.zipWithStream(other, zipper));
    }




    default <U> AnyMSeq<W,Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return fromIterable(IterableX.super.zipWithStream(other));
    }


    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return fromIterable(IterableX.super.zip(other));
    }


    @Override
    default <S, U> AnyMSeq<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return fromIterable(IterableX.super.zip3(second, third));
    }


    @Override
    default <T2, T3, T4> AnyMSeq<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return fromIterable(IterableX.super.zip4(second, third, fourth));
    }


    @Override
    default AnyMSeq<W,Tuple2<T, Long>> zipWithIndex() {

        return fromIterable(IterableX.super.zipWithIndex());
    }


    @Override
    default AnyMSeq<W,Seq<T>> sliding(final int windowSize) {

        return fromIterable(IterableX.super.sliding(windowSize));
    }


    @Override
    default AnyMSeq<W,Seq<T>> sliding(final int windowSize, final int increment) {

        return fromIterable(IterableX.super.sliding(windowSize, increment));
    }

    @Override
    default <C extends PersistentCollection<? super T>> AnyMSeq<W,C> grouped(final int size, final Supplier<C> supplier) {

        return fromIterable(IterableX.super.grouped(size, supplier));
    }


    @Override
    default AnyMSeq<W,Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return fromIterable(IterableX.super.groupedUntil(predicate));
    }

    @Override
    default AnyMSeq<W,Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return fromIterable(IterableX.super.groupedUntil(predicate));
    }


    @Override
    default AnyMSeq<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return fromIterable(IterableX.super.combine(predicate, op));
    }
    @Override
    default AnyMSeq<W,T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return fromIterable(IterableX.super.combine(op,predicate));
    }


    @Override
    default AnyMSeq<W,Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return fromIterable(IterableX.super.groupedWhile(predicate));
    }


    @Override
    default <C extends PersistentCollection<? super T>> AnyMSeq<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromIterable(IterableX.super.groupedWhile(predicate, factory));
    }


    @Override
    default <C extends PersistentCollection<? super T>> AnyMSeq<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromIterable(IterableX.super.groupedUntil(predicate, factory));
    }


    @Override
    default AnyMSeq<W,Vector<T>> grouped(final int groupSize) {

        return fromIterable(IterableX.super.grouped(groupSize));
    }



    @Override
    default AnyMSeq<W,T> takeWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.takeWhile(p));
    }


    @Override
    default AnyMSeq<W,T> dropWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.dropWhile(p));
    }


    @Override
    default AnyMSeq<W,T> takeUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.takeUntil(p));
    }


    @Override
    default AnyMSeq<W,T> dropUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.dropUntil(p));
    }


    @Override
    default AnyMSeq<W,T> dropRight(final int num) {

        return fromIterable(IterableX.super.dropRight(num));
    }

    @Override
    default AnyMSeq<W,T> takeRight(final int num) {

        return fromIterable(IterableX.super.takeRight(num));
    }


    @Override
    default AnyMSeq<W,T> reverse() {

        return fromIterable(IterableX.super.reverse());
    }


    @Override
    default AnyMSeq<W,T> shuffle() {

        return fromIterable(IterableX.super.shuffle());
    }


    @Override
    default AnyMSeq<W,T> shuffle(final Random random) {

        return fromIterable(IterableX.super.shuffle(random));
    }


    @Override
    default AnyMSeq<W,T> distinct() {

        return fromIterable(IterableX.super.distinct());
    }


    @Override
    default AnyMSeq<W,T> scanLeft(final Monoid<T> monoid) {

        return fromIterable(IterableX.super.scanLeft(monoid));
    }


    @Override
    default <U> AnyMSeq<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return fromIterable(IterableX.super.scanLeft(seed, function));
    }


    @Override
    default AnyMSeq<W,T> scanRight(final Monoid<T> monoid) {

        return fromIterable(IterableX.super.scanRight(monoid));
    }

    @Override
    default <U> AnyMSeq<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return fromIterable(IterableX.super.scanRight(identity, combiner));
    }


    @Override
    default AnyMSeq<W,T> sorted() {

        return fromIterable(IterableX.super.sorted());
    }


    @Override
    default AnyMSeq<W,T> sorted(final Comparator<? super T> c) {

        return fromIterable(IterableX.super.sorted(c));
    }


    @Override
    default AnyMSeq<W,T> skip(final long num) {

        return fromIterable(IterableX.super.skip(num));
    }


    @Override
    default AnyMSeq<W,T> skipWhile(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.skipWhile(p));
    }


    @Override
    default AnyMSeq<W,T> skipUntil(final Predicate<? super T> p) {

        return fromIterable(IterableX.super.skipUntil(p));
    }


    @Override
    default AnyMSeq<W,T> intersperse(final T value) {

        return fromIterable(IterableX.super.intersperse(value));
    }


    @Override
    default AnyMSeq<W,T> skipLast(final int num) {

        return fromIterable(IterableX.super.skipLast(num));
    }


    @Override
    default AnyMSeq<W,T> slice(final long from, final long to) {

        return fromIterable(IterableX.super.slice(from, to));
    }


    @Override
    default <U extends Comparable<? super U>> AnyMSeq<W,T> sorted(final Function<? super T, ? extends U> function) {

        return fromIterable(IterableX.super.sorted(function));
    }


    @Override
    default AnyMSeq<W,ReactiveSeq<T>> permutations() {

        return fromIterable(IterableX.super.permutations());
    }

    @Override
    default AnyMSeq<W,ReactiveSeq<T>> combinations(final int size) {

        return fromIterable(IterableX.super.combinations(size));
    }

    @Override
    default AnyMSeq<W,ReactiveSeq<T>> combinations() {

        return fromIterable(IterableX.super.combinations());
    }

    @Override
    default <U> AnyMSeq<W,U> ofType(final Class<? extends U> type) {

        return (AnyMSeq<W,U>) AnyM.super.ofType(type);
    }

    @Override
    default AnyMSeq<W, T> notNull() {
        return (AnyMSeq<W,T>) AnyM.super.notNull();
    }

    @Override
    default AnyMSeq<W,T> filterNot(final Predicate<? super T> fn) {
        return (AnyMSeq<W,T>) AnyM.super.filterNot(fn);
    }


    @Override
    default <U> AnyMSeq<W,U> unitIterable(Iterable<U> U){
        return (AnyMSeq<W,U>)adapter().unitIterable(U);
    }


    @Override
    default <T> AnyMSeq<W,T> emptyUnit(){
        return (AnyMSeq<W,T> )empty();
    }


    @Override
    default AnyMSeq<W,T> filter(Predicate<? super T> p){
        return (AnyMSeq<W,T>)AnyM.super.filter(p);
    }


    @Override
    default <R> AnyMSeq<W,R> map(Function<? super T, ? extends R> fn){
        return (AnyMSeq<W,R>)AnyM.super.map(fn);
    }


    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        return this.stream()
                   .forEach(numberOfElements, consumer);
    }


    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                       final Consumer<? super Throwable> consumerError) {
        return this.stream()
                   .forEach(numberOfElements, consumer, consumerError);
    }


    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                       final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return this.stream()
                   .forEach(numberOfElements, consumer, consumerError, onComplete);
    }


    @Override
    default <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        this.stream()
            .forEach(consumerElement, consumerError);

    }


    @Override
    default <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                               final Runnable onComplete) {
        this.stream()
            .forEach(consumerElement, consumerError, onComplete);

    }



    @Override
    default AnyMSeq<W,T> peek(final Consumer<? super T> c) {
        return map(i -> {
            c.accept(i);
            return i;
        });
    }


    static <W extends WitnessType<W>,T1> AnyMSeq<W,T1> flatten(AnyMSeq<W,AnyMSeq<W,T1>> nested){
        return nested.flatMap(Function.identity());
    }

    @Override
    default AnyMSeq<W,Seq<T>> aggregate(AnyM<W,T> next){
        return (AnyMSeq<W,Seq<T>>)AnyM.super.aggregate(next);
    }



    @Override
    default <R> AnyMSeq<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return (AnyMSeq<W,R>)AnyM.super.flatMapA(fn);
    }

    @Override
    default AnyMSeq<W,T> prependStream(Stream<? extends T> stream) {
        return fromIterable(IterableX.super.prependStream(stream));
    }

    @Override
    default AnyMSeq<W,T> appendAll(T... values) {
        return fromIterable(IterableX.super.appendAll(values));
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
    default AnyMSeq<W,T> prependAll(T... values) {
        return fromIterable(IterableX.super.prependAll(values));
    }

    @Override
    default AnyMSeq<W,T> insertAt(int pos, T... values) {
        return fromIterable(stream().insertAt(pos,values));
    }

    @Override
    default AnyMSeq<W,T> deleteBetween(int start, int end) {
        return fromIterable(IterableX.super.deleteBetween(start,end));
    }

    @Override
    default AnyMSeq<W,T> insertStreamAt(int pos, Stream<T> stream) {
        return fromIterable(IterableX.super.insertStreamAt(pos,stream));
    }

    @Override
    default AnyMSeq<W,T> recover(final Function<? super Throwable, ? extends T> fn) {
        final Object o = unwrap();
        if (o instanceof RecoverableTraversable) {
            RecoverableTraversable<T> rt = (RecoverableTraversable<T>)o;
            return fromIterable(rt.recover(fn));

        }
        return this;
    }

    @Override
    default <EX extends Throwable> AnyMSeq<W,T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        final Object o = unwrap();
        if (o instanceof RecoverableTraversable) {
            RecoverableTraversable<T> rt = (RecoverableTraversable<T>)o;
            return fromIterable(rt.recover(exceptionClass,fn));

        }
        return this;
    }

    @Override
    default <T> AnyMSeq<W,T> unit(T value){
        return (AnyMSeq<W,T>)AnyM.super.unit(value);
    }


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


    @Override
    default <T2, R> AnyMSeq<W,R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return fromIterable(IterableX.super.zip(fn, publisher));
    }

    @Override
    default <T2, R> AnyMSeq<W, R> zip(final AnyM<W, ? extends T2> anyM, final BiFunction<? super T, ? super T2, ? extends R> fn) {
      return (AnyMSeq<W, R>)AnyM.super.zip(anyM,fn);
    }

    @Override
    default <U> AnyMSeq<W, Tuple2<T, U>> zip(final AnyM<W, ? extends U> other) {
      return (AnyMSeq)AnyM.super.zip(other);
    }

    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return fromIterable(IterableX.super.zipWithPublisher(other));
    }


    @Override
    default <S, U, R> AnyMSeq<W,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return fromIterable(IterableX.super.zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> AnyMSeq<W,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return fromIterable(IterableX.super.zip4(second,third,fourth,fn));
    }


    @Override
    default AnyMSeq<W,T> removeStream(final Stream<? extends T> stream) {
        return fromIterable(IterableX.super.removeStream(stream));
    }



    @Override
    default AnyMSeq<W,T> removeAll(final T... values) {
        return fromIterable(IterableX.super.removeAll(values));
    }

    @Override
    default AnyMSeq<W,T> retainAll(final Iterable<? extends T> it) {
        return fromIterable(IterableX.super.retainAll(it));
    }

    @Override
    default AnyMSeq<W,T> retainStream(final Stream<? extends T> stream) {
        return fromIterable(IterableX.super.retainStream(stream));
    }

    @Override
    default AnyMSeq<W,T> retainAll(final T... values) {
        return fromIterable(IterableX.super.retainAll(values));
    }

    @Override
    default AnyMSeq<W,T> drop(final long num) {
        return fromIterable(IterableX.super.drop(num));
    }

    @Override
    default AnyMSeq<W,T> take(final long num) {
        return fromIterable(IterableX.super.take(num));
    }

    @Override
    default AnyMSeq<W,T> plusAll(Iterable<? extends T> list) {
        return fromIterable(IterableX.super.plusAll(list));
    }

    @Override
    default AnyMSeq<W,T> plus(T value) {
        return fromIterable(IterableX.super.plus(value));
    }

    @Override
    default AnyMSeq<W,T> removeValue(T value) {
        return fromIterable(IterableX.super.removeValue(value));
    }

    @Override
    default AnyMSeq<W,T> removeAt(long pos) {
        return fromIterable(IterableX.super.removeAt(pos));
    }

    @Override
    default AnyMSeq<W,T> removeAt(int pos) {
        return fromIterable(IterableX.super.removeAt(pos));
    }

    @Override
    default AnyMSeq<W,T> removeAll(Iterable<? extends T> value) {
        return fromIterable(IterableX.super.removeAll(value));
    }

    @Override
    default AnyMSeq<W,T> removeFirst(Predicate<? super T> pred) {
        return fromIterable(IterableX.super.removeFirst(pred));
    }

    @Override
    default AnyMSeq<W,T> appendAll(Iterable<? extends T> value) {
        return fromIterable(IterableX.super.appendAll(value));
    }

    @Override
    default AnyMSeq<W,T> prependAll(Iterable<? extends T> value) {
        return fromIterable(IterableX.super.prependAll(value));
    }


    @Override
    default AnyMSeq<W,T> updateAt(int pos, T value) {
        return fromIterable(stream().updateAt(pos,value));
    }



    @Override
    default AnyMSeq<W,T> insertAt(int i, T value) {
        return fromIterable(stream().insertAt(i,value));
    }

    @Override
    default AnyMSeq<W,T> insertAt(int pos, Iterable<? extends T> values) {
        return fromIterable(stream().insertAt(pos,values));
    }
}
