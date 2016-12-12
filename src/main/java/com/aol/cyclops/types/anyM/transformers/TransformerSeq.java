package com.aol.cyclops.types.anyM.transformers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Executor;
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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.ToStream;
import com.aol.cyclops.types.stream.future.FutureOperations;
import com.aol.cyclops.types.stream.lazy.LazyOperations;

public interface TransformerSeq<W extends WitnessType<W>,T> extends Unwrapable,ConvertableSequence<T>, Traversable<T>, Sequential<T>, Iterable<T>,

ToStream<T>, Publisher<T> {

    public boolean isSeqPresent();

    <T> TransformerSeq<W,T> unitAnyM(AnyM<W,Traversable<T>> traversable);

    AnyM<W,? extends Traversable<T>> transformerStream();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return unitAnyM(transformerStream().map(s -> s.combine(predicate, op)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {

        transformerStream().forEach(n -> n.subscribe(s));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(int)
     */
    @Override
    default Traversable<T> cycle(final int times) {
        return unitAnyM(transformerStream().map(s -> s.cycle(times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default Traversable<T> cycle(final Monoid<T> m, final int times) {
        return unitAnyM(transformerStream().map(s -> s.cycle(m, times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleWhile(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.cycleWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleUntil(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.cycleUntil(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        final AnyM<W,Traversable<R>> zipped = transformerStream().map(s -> s.zip(other, zipper));
        return unitAnyM(zipped);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>) other, zipper);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
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
        return unitAnyM(transformerStream().map(s -> s.zip(streamable.stream())));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Seq<? extends U> other) {
        return zip((Stream<? extends U>) other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
     */
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
        final AnyM<W,Traversable<Tuple3<T, S, U>>> zipped = transformerStream().map(s -> s.zip3(streamable2.stream(), streamable3.stream()));
        return unitAnyM(zipped);
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
        final AnyM<W,Traversable<Tuple4<T, T2, T3, T4>>> zipped = transformerStream().map(s -> s.zip4(streamable2.stream(), streamable3.stream(),
                                                                                                    streamable4.stream()));
        return unitAnyM(zipped);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipWithIndex()
     */
    @Override
    default Traversable<Tuple2<T, Long>> zipWithIndex() {
        return unitAnyM(transformerStream().map(s -> s.zipWithIndex()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int)
     */
    @Override
    default Traversable<ListX<T>> sliding(final int windowSize) {
        return unitAnyM(transformerStream().map(s -> s.sliding(windowSize)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int, int)
     */
    @Override
    default Traversable<ListX<T>> sliding(final int windowSize, final int increment) {
        return unitAnyM(transformerStream().map(s -> s.sliding(windowSize, increment)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> grouped(final int size, final Supplier<C> supplier) {
        return unitAnyM(transformerStream().map(s -> s.grouped(size, supplier)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedUntil(predicate)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default Traversable<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedStatefullyUntil(predicate)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s -> s.groupedWhile(predicate, factory)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s -> s.groupedUntil(predicate, factory)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int)
     */
    @Override
    default Traversable<ListX<T>> grouped(final int groupSize) {
        return unitAnyM(transformerStream().map(s -> s.grouped(groupSize)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> Traversable<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return unitAnyM(transformerStream().map(s -> s.grouped(classifier, downstream)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> Traversable<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return unitAnyM(transformerStream().map(s -> s.grouped(classifier)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#distinct()
     */
    @Override
    default Traversable<T> distinct() {
        return unitAnyM(transformerStream().map(s -> s.distinct()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default Traversable<T> scanLeft(final Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s -> s.scanLeft(monoid)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return unitAnyM(transformerStream().map(s -> s.scanLeft(seed, function)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default Traversable<T> scanRight(final Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s -> s.scanRight(monoid)));
    }

    /* (non-Javadoc)
     * @see com.aol).cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitAnyM(transformerStream().map(s -> s.scanRight(identity, combiner)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted()
     */
    @Override
    default Traversable<T> sorted() {
        return unitAnyM(transformerStream().map(s -> s.sorted()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default Traversable<T> sorted(final Comparator<? super T> c) {
        return unitAnyM(transformerStream().map(s -> s.sorted(c)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.takeWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.dropWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.takeUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.dropUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    default Traversable<T> dropRight(final int num) {
        return unitAnyM(transformerStream().map(s -> s.dropRight(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    default Traversable<T> takeRight(final int num) {
        return unitAnyM(transformerStream().map(s -> s.takeRight(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skip(long)
     */
    @Override
    default Traversable<T> skip(final long num) {
        return unitAnyM(transformerStream().map(s -> s.skip(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.skipWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.skipUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limit(long)
     */
    @Override
    default Traversable<T> limit(final long num) {
        return unitAnyM(transformerStream().map(s -> s.limit(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.limitWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.limitUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default Traversable<T> intersperse(final T value) {
        return unitAnyM(transformerStream().map(s -> s.intersperse(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#reverse()
     */
    @Override
    default Traversable<T> reverse() {
        return unitAnyM(transformerStream().map(s -> s.reverse()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle()
     */
    @Override
    default Traversable<T> shuffle() {
        return unitAnyM(transformerStream().map(s -> s.shuffle()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#futureOperations(java.util.concurrent.Executor)
     */
    @Override
    default FutureOperations<T> futureOperations(final Executor exec) {
        // TODO Auto-generated method stub
        return Traversable.super.futureOperations(exec);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#lazyOperations()
     */
    @Override
    default LazyOperations<T> lazyOperations() {
        // TODO Auto-generated method stub
        return Traversable.super.lazyOperations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipLast(int)
     */
    @Override
    default Traversable<T> skipLast(final int num) {
        return unitAnyM(transformerStream().map(s -> s.skipLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitLast(int)
     */
    @Override
    default Traversable<T> limitLast(final int num) {
        return unitAnyM(transformerStream().map(s -> s.limitLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default Traversable<T> onEmpty(final T value) {
        return unitAnyM(transformerStream().map(s -> s.onEmpty(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default Traversable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return unitAnyM(transformerStream().map(s -> s.onEmptyGet(supplier)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> Traversable<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return unitAnyM(transformerStream().map(s -> s.onEmptyThrow(supplier)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    default Traversable<T> shuffle(final Random random) {
        return unitAnyM(transformerStream().map(s -> s.shuffle(random)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#slice(long, long)
     */
    @Override
    default Traversable<T> slice(final long from, final long to) {
        return unitAnyM(transformerStream().map(s -> s.slice(from, to)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> Traversable<T> sorted(final Function<? super T, ? extends U> function) {
        return unitAnyM(transformerStream().map(s -> s.sorted(function)));
    }

    @Override
    default ReactiveSeq<T> stream() {
        return ConvertableSequence.super.stream();
    }

}
