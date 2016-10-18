package com.aol.cyclops.control.monads.transformers.values;

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

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Traversable;

public interface ValueTransformerSeq<T> extends TransformerSeq<T> {
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return unitStream(stream().combine(predicate, op));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {

        stream().subscribe(s);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(int)
     */
    @Override
    default Traversable<T> cycle(final int times) {
        return unitStream(stream().cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default Traversable<T> cycle(final Monoid<T> m, final int times) {
        return unitStream(stream().cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleWhile(final Predicate<? super T> predicate) {
        return unitStream(stream().cycleWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleUntil(final Predicate<? super T> predicate) {
        return unitStream(stream().cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        final ReactiveSeq<R> zipped = stream().zip(other, zipper);
        return unitStream(zipped);

    }

    @Override
    default <U, R> Traversable<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>) other, zipper);

    }

    @Override
    default <U, R> Traversable<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>) ReactiveSeq.fromStream(other), zipper);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Stream<? extends U> other) {
        final Streamable<? extends U> streamable = Streamable.fromStream(other);
        return unitStream(stream().zip(streamable.stream()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Seq<? extends U> other) {
        return zip((Stream<? extends U>) other);
    }

    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return zip((Stream<? extends U>) ReactiveSeq.fromIterable(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> Traversable<Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {
        final Streamable<? extends S> streamable2 = Streamable.fromStream(second);
        final Streamable<? extends U> streamable3 = Streamable.fromStream(third);
        final ReactiveSeq<Tuple3<T, S, U>> zipped = stream().zip3(streamable2.stream(), streamable3.stream());
        return unitStream(zipped);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> Traversable<Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {
        final Streamable<? extends T2> streamable2 = Streamable.fromStream(second);
        final Streamable<? extends T3> streamable3 = Streamable.fromStream(third);
        final Streamable<? extends T4> streamable4 = Streamable.fromStream(fourth);
        final ReactiveSeq<Tuple4<T, T2, T3, T4>> zipped = stream().zip4(streamable2.stream(), streamable3.stream(), streamable4.stream());
        return unitStream(zipped);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipWithIndex()
     */
    @Override
    default Traversable<Tuple2<T, Long>> zipWithIndex() {
        return unitStream(stream().zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int)
     */
    @Override
    default Traversable<ListX<T>> sliding(final int windowSize) {
        return unitStream(stream().sliding(windowSize));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int, int)
     */
    @Override
    default Traversable<ListX<T>> sliding(final int windowSize, final int increment) {
        return unitStream(stream().sliding(windowSize, increment));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> grouped(final int size, final Supplier<C> supplier) {
        return unitStream(stream().grouped(size, supplier));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default Traversable<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitStream(stream().groupedStatefullyUntil(predicate));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return unitStream(stream().groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitStream(stream().groupedWhile(predicate, factory));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitStream(stream().groupedUntil(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int)
     */
    @Override
    default Traversable<ListX<T>> grouped(final int groupSize) {
        return unitStream(stream().grouped(groupSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> Traversable<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return unitStream(stream().grouped(classifier, downstream));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> Traversable<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return unitStream(stream().grouped(classifier));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#distinct()
     */
    @Override
    default Traversable<T> distinct() {
        return unitStream(stream().distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default Traversable<T> scanLeft(final Monoid<T> monoid) {
        return unitStream(stream().scanLeft(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return unitStream(stream().scanLeft(seed, function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default Traversable<T> scanRight(final Monoid<T> monoid) {
        return unitStream(stream().scanRight(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol).cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitStream(stream().scanRight(identity, combiner));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted()
     */
    @Override
    default Traversable<T> sorted() {
        return unitStream(stream().sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default Traversable<T> sorted(final Comparator<? super T> c) {
        return unitStream(stream().sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeWhile(final Predicate<? super T> p) {
        return unitStream(stream().takeWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropWhile(final Predicate<? super T> p) {
        return unitStream(stream().dropWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeUntil(final Predicate<? super T> p) {
        return unitStream(stream().takeUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return unitStream(stream().dropUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    default Traversable<T> dropRight(final int num) {
        return unitStream(stream().dropRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    default Traversable<T> takeRight(final int num) {
        return unitStream(stream().takeRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skip(long)
     */
    @Override
    default Traversable<T> skip(final long num) {
        return unitStream(stream().skip(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipWhile(final Predicate<? super T> p) {
        return unitStream(stream().skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipUntil(final Predicate<? super T> p) {
        return unitStream(stream().skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limit(long)
     */
    @Override
    default Traversable<T> limit(final long num) {
        return unitStream(stream().limit(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitWhile(final Predicate<? super T> p) {
        return unitStream(stream().limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitUntil(final Predicate<? super T> p) {
        return unitStream(stream().limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default Traversable<T> intersperse(final T value) {
        return unitStream(stream().intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#reverse()
     */
    @Override
    default Traversable<T> reverse() {
        return unitStream(stream().reverse());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle()
     */
    @Override
    default Traversable<T> shuffle() {
        return unitStream(stream().shuffle());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipLast(int)
     */
    @Override
    default Traversable<T> skipLast(final int num) {
        return unitStream(stream().skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitLast(int)
     */
    @Override
    default Traversable<T> limitLast(final int num) {
        return unitStream(stream().limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default Traversable<T> onEmpty(final T value) {
        return unitStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default Traversable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return unitStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> Traversable<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return unitStream(stream().onEmptyThrow(supplier));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    default Traversable<T> shuffle(final Random random) {
        return unitStream(stream().shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#slice(long, long)
     */
    @Override
    default Traversable<T> slice(final long from, final long to) {
        return unitStream(stream().slice(from, to));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> Traversable<T> sorted(final Function<? super T, ? extends U> function) {
        return unitStream(stream().sorted(function));
    }

    <T> TransformerSeq<T> unitStream(ReactiveSeq<T> traversable);

}
