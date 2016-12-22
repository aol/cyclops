package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cyclops.Streams;
import cyclops.collections.ListX;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;

public interface MutableCollectionX<T> extends FluentCollectionX<T> {
    
    
   

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        return stream().reduce(identity, accumulator);
    }

    /**
     * @param stream Create a MultableCollectionX from a Stream
     * @return MutableCollectionX
     */
    <X> MutableCollectionX<X> fromStream(Stream<X> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default MutableCollectionX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return fromStream(stream().combine(predicate, op));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#reverse()
     */
    @Override
    default MutableCollectionX<T> reverse() {
        return fromStream(stream().reverse());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> filter(final Predicate<? super T> pred) {
        return fromStream(stream().filter(pred));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> CollectionX<R> map(final Function<? super T, ? extends R> mapper) {
        return fromStream(stream().map(mapper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> CollectionX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return fromStream(stream().flatMap(mapper.andThen(Streams::stream)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#limit(long)
     */
    @Override
    default MutableCollectionX<T> limit(final long num) {
        return fromStream(stream().limit(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#skip(long)
     */
    @Override
    default MutableCollectionX<T> skip(final long num) {
        return fromStream(stream().skip(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#takeRight(int)
     */
    @Override
    default MutableCollectionX<T> takeRight(final int num) {
        return fromStream(stream().limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#dropRight(int)
     */
    @Override
    default MutableCollectionX<T> dropRight(final int num) {
        return fromStream(stream().skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> takeWhile(final Predicate<? super T> p) {
        return fromStream(stream().limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> dropWhile(final Predicate<? super T> p) {
        return fromStream(stream().skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> takeUntil(final Predicate<? super T> p) {
        return fromStream(stream().limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> dropUntil(final Predicate<? super T> p) {
        return fromStream(stream().skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> MutableCollectionX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return fromStream(stream().trampoline(mapper));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#slice(long, long)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#slice(long, long)
     */
    @Override
    default MutableCollectionX<T> slice(final long from, final long to) {
        return fromStream(stream().slice(from, to));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(int)
     */
    @Override
    default MutableCollectionX<ListX<T>> grouped(final int groupSize) {
        return fromStream(stream().grouped(groupSize)
                                  .map(ListX::fromIterable));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> MutableCollectionX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return fromStream(stream().grouped(classifier, downstream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(java.util.function.Function)
     */
    @Override
    default <K> MutableCollectionX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return fromStream(stream().grouped(classifier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> MutableCollectionX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return fromStream(stream().zip(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> MutableCollectionX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return fromStream(stream().zip(other, zipper));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> MutableCollectionX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return fromStream(stream().zipS(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#sliding(int)
     */
    @Override
    default MutableCollectionX<ListX<T>> sliding(final int windowSize) {
        return fromStream(stream().sliding(windowSize)
                                  .map(ListX::fromIterable));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#sliding(int, int)
     */
    @Override
    default MutableCollectionX<ListX<T>> sliding(final int windowSize, final int increment) {
        return fromStream(stream().sliding(windowSize, increment)
                                  .map(ListX::fromIterable));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanLeft(cyclops.function.Monoid)
     */
    @Override
    default MutableCollectionX<T> scanLeft(final Monoid<T> monoid) {
        return fromStream(stream().scanLeft(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> MutableCollectionX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return fromStream(stream().scanLeft(seed, function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanRight(cyclops.function.Monoid)
     */
    @Override
    default MutableCollectionX<T> scanRight(final Monoid<T> monoid) {
        return fromStream(stream().scanRight(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> MutableCollectionX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return fromStream(stream().scanRight(identity, combiner));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> MutableCollectionX<T> sorted(final Function<? super T, ? extends U> function) {
        return fromStream(stream().sorted(function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#plus(java.lang.Object)
     */
    @Override
    default MutableCollectionX<T> plus(final T e) {
        add(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#plusAll(java.util.Collection)
     */
    @Override
    default MutableCollectionX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#minus(java.lang.Object)
     */
    @Override
    default MutableCollectionX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#minusAll(java.util.Collection)
     */
    @Override
    default MutableCollectionX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycle(int)
     */
    @Override
    default MutableCollectionX<T> cycle(final int times) {

        return fromStream(stream().cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycle(cyclops.function.Monoid, int)
     */
    @Override
    default MutableCollectionX<T> cycle(final Monoid<T> m, final int times) {

        return fromStream(stream().cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> cycleWhile(final Predicate<? super T> predicate) {

        return fromStream(stream().cycleWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> cycleUntil(final Predicate<? super T> predicate) {

        return fromStream(stream().cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> MutableCollectionX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return fromStream(stream().zipS(other));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> MutableCollectionX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return fromStream(stream().zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> MutableCollectionX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return fromStream(stream().zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#zipWithIndex()
     */
    @Override
    default MutableCollectionX<Tuple2<T, Long>> zipWithIndex() {

        return fromStream(stream().zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#distinct()
     */
    @Override
    default MutableCollectionX<T> distinct() {

        return fromStream(stream().distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#sorted()
     */
    @Override
    default MutableCollectionX<T> sorted() {

        return fromStream(stream().sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#sorted(java.util.Comparator)
     */
    @Override
    default MutableCollectionX<T> sorted(final Comparator<? super T> c) {

        return fromStream(stream().sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> skipWhile(final Predicate<? super T> p) {

        return fromStream(stream().skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> skipUntil(final Predicate<? super T> p) {

        return fromStream(stream().skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> limitWhile(final Predicate<? super T> p) {

        return fromStream(stream().limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> limitUntil(final Predicate<? super T> p) {

        return fromStream(stream().limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#intersperse(java.lang.Object)
     */
    @Override
    default MutableCollectionX<T> intersperse(final T value) {

        return fromStream(stream().intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#shuffle()
     */
    @Override
    default MutableCollectionX<T> shuffle() {

        return fromStream(stream().shuffle());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#skipLast(int)
     */
    @Override
    default MutableCollectionX<T> skipLast(final int num) {

        return fromStream(stream().skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#limitLast(int)
     */
    @Override
    default MutableCollectionX<T> limitLast(final int num) {

        return fromStream(stream().limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default MutableCollectionX<T> onEmpty(final T value) {
        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default MutableCollectionX<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return fromStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> MutableCollectionX<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return fromStream(stream().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#shuffle(java.util.Random)
     */
    @Override
    default MutableCollectionX<T> shuffle(final Random random) {
        return fromStream(stream().shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> MutableCollectionX<U> ofType(final Class<? extends U> type) {

        return (MutableCollectionX) FluentCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<T> filterNot(final Predicate<? super T> fn) {
        return fromStream(stream().filterNot(fn));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#notNull()
     */
    @Override
    default MutableCollectionX<T> notNull() {
        return fromStream(stream().notNull());

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default MutableCollectionX<T> removeAll(final Stream<? extends T> stream) {

        return fromStream(stream().removeAll(stream));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default MutableCollectionX<T> removeAll(final Iterable<? extends T> it) {
        return fromStream(stream().removeAll(it));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default MutableCollectionX<T> removeAll(final T... values) {
        return fromStream(stream().removeAll(values));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    default MutableCollectionX<T> retainAll(final Iterable<? extends T> it) {
        return fromStream(stream().retainAll(it));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    default MutableCollectionX<T> retainAll(final Stream<? extends T> stream) {
        return fromStream(stream().retainAll(stream));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(java.lang.Object[])
     */
    @Override
    default MutableCollectionX<T> retainAll(final T... values) {
        return fromStream(stream().retainAll(values));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> MutableCollectionX<U> cast(final Class<? extends U> type) {
        return fromStream(stream().cast(type));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#permutations()
     */
    @Override
    default MutableCollectionX<ReactiveSeq<T>> permutations() {
        return fromStream(stream().permutations());

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#combinations(int)
     */
    @Override
    default MutableCollectionX<ReactiveSeq<T>> combinations(final int size) {
        return fromStream(stream().combinations(size));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#combinations()
     */
    @Override
    default MutableCollectionX<ReactiveSeq<T>> combinations() {
        return fromStream(stream().combinations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> grouped(final int size, final Supplier<C> supplier) {

        return fromStream(stream().grouped(size, supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return fromStream(stream().groupedUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return fromStream(stream().groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromStream(stream().groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromStream(stream().groupedUntil(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default MutableCollectionX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return fromStream(stream().groupedStatefullyUntil(predicate));
    }

}
