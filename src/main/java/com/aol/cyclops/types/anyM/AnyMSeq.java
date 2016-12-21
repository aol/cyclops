package com.aol.cyclops.types.anyM;

import static com.aol.cyclops.internal.Utils.firstOrNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.types.*;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.extensability.FunctionalAdapter;
import com.aol.cyclops.util.function.Predicates;
import com.aol.cyclops.util.function.F4;
import com.aol.cyclops.util.function.F3;

/**
 * Wrapper around 'Any' non-scalar 'M'onad
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of elements managed by wrapped non-scalar Monad.
 */
public interface AnyMSeq<W extends WitnessType<W>,T> extends AnyM<W,T>, FoldableTraversable<T>, Publisher<T> {


    default <R> AnyM<W,R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn){
        return this.flatMap(fn.andThen(i->unitIterator(i.iterator())));
    }
    default <R> AnyM<W,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn){
        return this.flatMap(fn.andThen(i->unitIterator(ReactiveSeq.fromPublisher(i).iterator())));
    }
    default <R> AnyM<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn){
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
                            final F3<? super T, ? super R1, ? super R2, ? extends AnyM<W,R3>> monad3,
                            final F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
       
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
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R3,R> AnyMSeq<W,R> forEach4(final Function<? super T, ? extends AnyM<W,R1>> monad1,
            final BiFunction<? super T,? super R1, ? extends AnyM<W,R2>> monad2,
                    final F3<? super T, ? super R1, ? super R2, ? extends AnyM<W,R3>> monad3,
                        final F4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                final F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){

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
     * @return LazyFutureStream with elements generated via nested iteration
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
     * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
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
     * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
     * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
     * @return AnyM with elements generated via nested iteration
     */
    default <R1, R2, R> AnyMSeq<W,R> forEach3(Function<? super T, ? extends AnyM<W,R1>> monad1,
            BiFunction<? super T, ? super R1, ? extends AnyM<W,R2>> monad2,
            F3<? super T,? super R1, ? super R2, Boolean> filterFunction,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){

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
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
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
     * @see com.aol.cyclops.control.AnyM#collect(java.util.stream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream().collect(collector);

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#traversable()
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
     * @see com.aol.cyclops.types.Traversable#limit(long)
     */
    @Override
    default AnyMSeq<W,T> limit(final long num) {

        return fromIterable(FoldableTraversable.super.limit(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> limitWhile(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> limitUntil(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitLast(int)
     */
    @Override
    default AnyMSeq<W,T> limitLast(final int num) {

        return fromIterable(FoldableTraversable.super.limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default AnyMSeq<W,T> onEmpty(final T value) {
        return fromIterable(FoldableTraversable.super.onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default AnyMSeq<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return fromIterable(FoldableTraversable.super.onEmptyGet(supplier));
    }
    FunctionalAdapter<W> adapter();
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> AnyMSeq<W,T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return fromIterable(FoldableTraversable.super.onEmptyThrow(supplier));
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
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

    
    /**
     * @return The first value of this monad
     */
    default Value<T> toFirstValue() {

        return () -> firstOrNull(toListX());
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> AnyMSeq<W,U> cast(final Class<? extends U> type) {

        return (AnyMSeq<W,U>) FoldableTraversable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMSeq<W,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMSeq<W,R>) FoldableTraversable.super.trampoline(mapper);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(int)
     */
    @Override
    default AnyMSeq<W,T> cycle(final int times) {

        return fromIterable(FoldableTraversable.super.cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default AnyMSeq<W,T> cycle(final Monoid<T> m, final int times) {

        return fromIterable(FoldableTraversable.super.cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> cycleWhile(final Predicate<? super T> predicate) {
        
        return fromIterable(FoldableTraversable.super.cycleWhile(predicate));
    }
    @Override
    default Xor<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Xor.primary(this);
    }
    @Override
    default <T> AnyMSeq<W,T> fromIterable(Iterable<T> t){
        return  (AnyMSeq<W,T>)adapter().unitIterable(t);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return fromIterable(FoldableTraversable.super.cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return fromIterable(FoldableTraversable.super.zip(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<W,R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return fromIterable(FoldableTraversable.super.zipS(other, zipper));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return fromIterable(FoldableTraversable.super.zipS(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable)
     */
    @Override
    default <U> AnyMSeq<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return fromIterable(FoldableTraversable.super.zip(other));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> AnyMSeq<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return fromIterable(FoldableTraversable.super.zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> AnyMSeq<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return fromIterable(FoldableTraversable.super.zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipWithIndex()
     */
    @Override
    default AnyMSeq<W,Tuple2<T, Long>> zipWithIndex() {

        return fromIterable(FoldableTraversable.super.zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int)
     */
    @Override
    default AnyMSeq<W,ListX<T>> sliding(final int windowSize) {

        return fromIterable(FoldableTraversable.super.sliding(windowSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int, int)
     */
    @Override
    default AnyMSeq<W,ListX<T>> sliding(final int windowSize, final int increment) {

        return fromIterable(FoldableTraversable.super.sliding(windowSize, increment));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<W,C> grouped(final int size, final Supplier<C> supplier) {

        return fromIterable(FoldableTraversable.super.grouped(size, supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return fromIterable(FoldableTraversable.super.groupedUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default AnyMSeq<W,ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return fromIterable(FoldableTraversable.super.groupedStatefullyUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default AnyMSeq<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return fromIterable(FoldableTraversable.super.combine(predicate, op));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return fromIterable(FoldableTraversable.super.groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromIterable(FoldableTraversable.super.groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromIterable(FoldableTraversable.super.groupedUntil(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int)
     */
    @Override
    default AnyMSeq<W,ListX<T>> grouped(final int groupSize) {

        return fromIterable(FoldableTraversable.super.grouped(groupSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> AnyMSeq<W,Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {

        return fromIterable(FoldableTraversable.super.grouped(classifier, downstream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> AnyMSeq<W,Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return fromIterable(FoldableTraversable.super.grouped(classifier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> takeWhile(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.takeWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> dropWhile(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.dropWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> takeUntil(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.takeUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> dropUntil(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.dropUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    default AnyMSeq<W,T> dropRight(final int num) {

        return fromIterable(FoldableTraversable.super.dropRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    default AnyMSeq<W,T> takeRight(final int num) {

        return fromIterable(FoldableTraversable.super.takeRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#reverse()
     */
    @Override
    default AnyMSeq<W,T> reverse() {

        return fromIterable(FoldableTraversable.super.reverse());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle()
     */
    @Override
    default AnyMSeq<W,T> shuffle() {

        return fromIterable(FoldableTraversable.super.shuffle());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    default AnyMSeq<W,T> shuffle(final Random random) {

        return fromIterable(FoldableTraversable.super.shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#distinct()
     */
    @Override
    default AnyMSeq<W,T> distinct() {

        return fromIterable(FoldableTraversable.super.distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default AnyMSeq<W,T> scanLeft(final Monoid<T> monoid) {

        return fromIterable(FoldableTraversable.super.scanLeft(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> AnyMSeq<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return fromIterable(FoldableTraversable.super.scanLeft(seed, function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default AnyMSeq<W,T> scanRight(final Monoid<T> monoid) {

        return fromIterable(FoldableTraversable.super.scanRight(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> AnyMSeq<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return fromIterable(FoldableTraversable.super.scanRight(identity, combiner));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted()
     */
    @Override
    default AnyMSeq<W,T> sorted() {

        return fromIterable(FoldableTraversable.super.sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default AnyMSeq<W,T> sorted(final Comparator<? super T> c) {

        return fromIterable(FoldableTraversable.super.sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skip(long)
     */
    @Override
    default AnyMSeq<W,T> skip(final long num) {

        return fromIterable(FoldableTraversable.super.skip(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> skipWhile(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> skipUntil(final Predicate<? super T> p) {

        return fromIterable(FoldableTraversable.super.skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default AnyMSeq<W,T> intersperse(final T value) {

        return fromIterable(FoldableTraversable.super.intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipLast(int)
     */
    @Override
    default AnyMSeq<W,T> skipLast(final int num) {

        return fromIterable(FoldableTraversable.super.skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#slice(long, long)
     */
    @Override
    default AnyMSeq<W,T> slice(final long from, final long to) {

        return fromIterable(FoldableTraversable.super.slice(from, to));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> AnyMSeq<W,T> sorted(final Function<? super T, ? extends U> function) {

        return fromIterable(FoldableTraversable.super.sorted(function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#permutations()
     */
    @Override
    default AnyMSeq<W,ReactiveSeq<T>> permutations() {

        return fromIterable(FoldableTraversable.super.permutations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#combinations(int)
     */
    @Override
    default AnyMSeq<W,ReactiveSeq<T>> combinations(final int size) {

        return fromIterable(FoldableTraversable.super.combinations(size));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#combinations()
     */
    @Override
    default AnyMSeq<W,ReactiveSeq<T>> combinations() {

        return fromIterable(FoldableTraversable.super.combinations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMSeq<W,U> ofType(final Class<? extends U> type) {

        return (AnyMSeq<W,U>) FoldableTraversable.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> filterNot(final Predicate<? super T> fn) {
        return (AnyMSeq<W,T>) FoldableTraversable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <U> AnyMSeq<W,U> unitIterator(Iterator<U> U){
        return (AnyMSeq<W,U>)adapter().unitIterable(()->U);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMSeq<W,T> emptyUnit(){
        return (AnyMSeq<W,T> )empty();
    }

    


    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<W,T> filter(Predicate<? super T> p){
        return (AnyMSeq<W,T>)AnyM.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#map(java.util.function.Function)
     */
    @Override
    default <R> AnyMSeq<W,R> map(Function<? super T, ? extends R> fn){
        return (AnyMSeq<W,R>)AnyM.super.map(fn);
    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachX(long, java.util.function.Consumer)
     */
    @Override
    default <X extends Throwable> Subscription forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return this.stream()
                   .forEachX(numberOfElements, consumer);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachXWithError(long, java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default <X extends Throwable> Subscription forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError) {
        return this.stream()
                   .forEachXWithError(numberOfElements, consumer, consumerError);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachXEvents(long, java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
     */
    @Override
    default <X extends Throwable> Subscription forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return this.stream()
                   .forEachXEvents(numberOfElements, consumer, consumerError, onComplete);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachWithError(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default <X extends Throwable> void forEachWithError(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        this.stream()
            .forEachWithError(consumerElement, consumerError);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations#forEachEvent(java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
     */
    @Override
    default <X extends Throwable> void forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        this.stream()
            .forEachEvent(consumerElement, consumerError, onComplete);

    }


    /* (non-Javadoc)
         * @see com.aol.cyclops.monad.AnyM#peek(java.util.function.Consumer)
         */
    @Override
    default AnyMSeq<W,T> peek(final Consumer<? super T> c) {
        return map(i -> {
            c.accept(i);
            return i;
        });
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#flatten()
     */
    
    static <W extends WitnessType<W>,T1> AnyMSeq<W,T1> flatten(AnyMSeq<W,AnyMSeq<W,T1>> nested){
        return nested.flatMap(Function.identity());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#aggregate(com.aol.cyclops.monad.AnyM)
     */
    @Override
    default AnyMSeq<W,List<T>> aggregate(AnyM<W,T> next){
        return (AnyMSeq<W,List<T>>)AnyM.super.aggregate(next);
    }
    
  

    @Override
    default <R> AnyMSeq<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return (AnyMSeq<W,R>)AnyM.super.flatMapA(fn);
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMSeq<W,T> unit(T value){
        return (AnyMSeq<W,T>)AnyM.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#empty()
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

}
