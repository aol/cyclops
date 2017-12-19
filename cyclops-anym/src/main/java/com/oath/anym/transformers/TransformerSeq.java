package com.oath.anym.transformers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;

import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import com.oath.cyclops.types.Unwrapable;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import com.oath.cyclops.types.stream.ToStream;
import cyclops.control.Trampoline;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.reactive.collections.immutable.VectorX;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.WitnessType;

public interface TransformerSeq<W extends WitnessType<W>,T> extends Unwrapable,
                                                                    Traversable<T>,
                                                                    ToStream<T>,
                                                                    Publisher<T> {

    default ConvertableSequence<T> to(){
        return new ConvertableSequence<>(this);
    }

    public boolean isSeqPresent();

    <T> TransformerSeq<W,T> unitAnyM(AnyM<W,Traversable<T>> traversable);

    AnyM<W,? extends IterableX<T>> transformerStream();


    @Override
    default Traversable<T> prependStream(Stream<? extends T> stream){
        return unitAnyM(transformerStream().map(s -> s.prependStream(stream)));
    }

    @Override
    default Traversable<T> appendAll(T... values){
        return unitAnyM(transformerStream().map(s -> s.appendAll(values)));
    }

    @Override
    default Traversable<T> appendAll(T value){
        return unitAnyM(transformerStream().map(s -> s.appendAll(value)));
    }

    @Override
    default Traversable<T> prepend(T value){
        return unitAnyM(transformerStream().map(s -> s.prepend(value)));
    }

    @Override
    default Traversable<T> prependAll(T... values){
        return unitAnyM(transformerStream().map(s -> s.prependAll(values)));
    }

    @Override
    default Traversable<T> insertAt(int pos, T... values){
        return unitAnyM(transformerStream().map(s -> s.insertAt(pos,values)));
    }

    @Override
    default Traversable<T> deleteBetween(int start, int end){
        return unitAnyM(transformerStream().map(s -> s.deleteBetween(start,end)));
    }

    @Override
    default Traversable<T> insertStreamAt(int pos, Stream<T> stream){
        return unitAnyM(transformerStream().map(s -> s.insertStreamAt(pos,stream)));
    }

    /* (non-Javadoc)
         * @see com.oath.cyclops.types.traversable.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
         */
    @Override
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return unitAnyM(transformerStream().map(s -> s.combine(predicate, op)));
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {

        transformerStream().forEach(n -> n.subscribe(s));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#cycle(int)
     */
    @Override
    default Traversable<T> cycle(final long times) {
        return unitAnyM(transformerStream().map(s -> s.cycle(times)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#cycle(cyclops2.function.Monoid, int)
     */
    @Override
    default Traversable<T> cycle(final Monoid<T> m, final long times) {
        return unitAnyM(transformerStream().map(s -> s.cycle(m, times)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleWhile(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.cycleWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleUntil(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.cycleUntil(predicate)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        final AnyM<W,Traversable<R>> zipped = transformerStream().map(s -> s.zip(other, zipper));
        return unitAnyM(zipped);

    }



    @Override
    default Traversable<T> removeStream(final Stream<? extends T> stream) {
        final AnyM<W,Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.removeStream(stream));
        return unitAnyM(zipped);
    }


    @Override
    default <U> Traversable<U> ofType(final Class<? extends U> type) {
        AnyM<W, Traversable<U>> zipped = transformerStream().map(s -> (Traversable)s.ofType(type));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> removeAll(final Iterable<? extends T> it) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable) this.removeAll(it));
        return unitAnyM(zipped);
    }


    @Override
    default Traversable<T> removeAll(final T... values) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.removeAll(values));
        return unitAnyM(zipped);
    }


    @Override
    default Traversable<T> filterNot(final Predicate<? super T> predicate) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.filterNot(predicate));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> peek(final Consumer<? super T> c) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.peek(c));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> retainAll(final Iterable<? extends T> it) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable) s.retainAll(it));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> notNull() {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.notNull());
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> retainStream(final Stream<? extends T> stream) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.retainStream(stream));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> retainAll(final T... values) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> (Traversable)s.retainAll(values));
        return unitAnyM(zipped);
    }

    @Override
    default <R> Traversable<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        AnyM<W, Traversable<R>> zipped = transformerStream().map(s -> (Traversable)s.trampoline(mapper));
        return unitAnyM(zipped);
    }

    @Override
    default <R> Traversable<R> retry(final Function<? super T, ? extends R> fn) {
        AnyM<W, Traversable<R>> zipped = transformerStream().map(s -> (Traversable)s.retry(fn));
        return unitAnyM(zipped);
    }

    @Override
    default <R> Traversable<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        AnyM<W, Traversable<R>> zipped = transformerStream().map(s -> (Traversable)s.retry(fn,retries,delay,timeUnit));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> s.combine(op,predicate));
        return unitAnyM(zipped);
    }

    @Override
    default Traversable<T> drop(final long num) {
        return skip(num);
    }

    @Override
    default Traversable<T> take(final long num) {
        return limit(num);
    }


    @Override
    default Traversable<T> recover(final Function<? super Throwable, ? extends T> fn) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> s.recover(fn));
        return unitAnyM(zipped);
    }

    @Override
    default <EX extends Throwable> Traversable<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        AnyM<W, Traversable<T>> zipped = transformerStream().map(s -> s.recover(exceptionClass,fn));
        return unitAnyM(zipped);
    }

  @Override
    default <T2, R> Traversable<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        AnyM<W, Traversable<R>> zipped = transformerStream().map(s -> s.zip(fn, publisher));
        return unitAnyM(zipped);
    }

    @Override
    default <U> Traversable<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        AnyM<W, Traversable<Tuple2<T, U>>> zipped = transformerStream().map(s -> s.zipWithPublisher(other));
        return unitAnyM(zipped);
    }

    @Override
    default <S, U, R> Traversable<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        AnyM<W, Traversable<R>> zipped = transformerStream().map(s -> s.zip3(second, third, fn3));
        return unitAnyM(zipped);
    }

    @Override
    default <T2, T3, T4, R> Traversable<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        AnyM<W, Traversable<R>> zipped = transformerStream().map(s -> s.zip4(second, third,fourth, fn));
        return unitAnyM(zipped);
    }


    default <U, R> Traversable<R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>) ReactiveSeq.fromStream(other), zipper);

    }


    default <U> Traversable<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {
        final Streamable<? extends U> streamable = Streamable.fromStream(other);
        return unitAnyM(transformerStream().map(s -> s.zipWithStream(streamable.stream())));
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return zipWithStream((Stream<? extends U>) ReactiveSeq.fromIterable(other));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> Traversable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        final Streamable<? extends S> streamable2 = Streamable.fromIterable(second);
        final Streamable<? extends U> streamable3 = Streamable.fromIterable(third);
        final AnyM<W,Traversable<Tuple3<T, S, U>>> zipped = transformerStream().map(s -> s.zip3(streamable2.stream(), streamable3.stream()));
        return unitAnyM(zipped);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> Traversable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {
        final Streamable<? extends T2> streamable2 = Streamable.fromIterable(second);
        final Streamable<? extends T3> streamable3 = Streamable.fromIterable(third);
        final Streamable<? extends T4> streamable4 = Streamable.fromIterable(fourth);
        final AnyM<W,Traversable<Tuple4<T, T2, T3, T4>>> zipped = transformerStream().map(s -> s.zip4(streamable2.stream(), streamable3.stream(),
                                                                                                    streamable4.stream()));
        return unitAnyM(zipped);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zipWithIndex()
     */
    @Override
    default Traversable<Tuple2<T, Long>> zipWithIndex() {
        return unitAnyM(transformerStream().map(s -> s.zipWithIndex()));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#sliding(int)
     */
    @Override
    default Traversable<VectorX<T>> sliding(final int windowSize) {
        return unitAnyM(transformerStream().map(s -> s.sliding(windowSize)));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#sliding(int, int)
     */
    @Override
    default Traversable<VectorX<T>> sliding(final int windowSize, final int increment) {
        return unitAnyM(transformerStream().map(s -> s.sliding(windowSize, increment)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> grouped(final int size, final Supplier<C> supplier) {
        return unitAnyM(transformerStream().map(s -> s.grouped(size, supplier)));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedUntil(predicate)));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default Traversable<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedStatefullyUntil(predicate)));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s -> s.groupedWhile(predicate, factory)));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s -> s.groupedUntil(predicate, factory)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#grouped(int)
     */
    @Override
    default Traversable<ListX<T>> grouped(final int groupSize) {
        return unitAnyM(transformerStream().map(s -> s.grouped(groupSize)));
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#distinct()
     */
    @Override
    default Traversable<T> distinct() {
        return unitAnyM(transformerStream().map(s -> s.distinct()));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default Traversable<T> scanLeft(final Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s -> s.scanLeft(monoid)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return unitAnyM(transformerStream().map(s -> s.scanLeft(seed, function)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default Traversable<T> scanRight(final Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s -> s.scanRight(monoid)));
    }

    /* (non-Javadoc)
     * @see com.aol).cyclops2.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitAnyM(transformerStream().map(s -> s.scanRight(identity, combiner)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#sorted()
     */
    @Override
    default Traversable<T> sorted() {
        return unitAnyM(transformerStream().map(s -> s.sorted()));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default Traversable<T> sorted(final Comparator<? super T> c) {
        return unitAnyM(transformerStream().map(s -> s.sorted(c)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.takeWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.dropWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.takeUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.dropUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#dropRight(int)
     */
    @Override
    default Traversable<T> dropRight(final int num) {
        return unitAnyM(transformerStream().map(s -> s.dropRight(num)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#takeRight(int)
     */
    @Override
    default Traversable<T> takeRight(final int num) {
        return unitAnyM(transformerStream().map(s -> s.takeRight(num)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#skip(long)
     */
    @Override
    default Traversable<T> skip(final long num) {
        return unitAnyM(transformerStream().map(s -> s.skip(num)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.skipWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.skipUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#limit(long)
     */
    @Override
    default Traversable<T> limit(final long num) {
        return unitAnyM(transformerStream().map(s -> s.limit(num)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.limitWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.limitUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default Traversable<T> intersperse(final T value) {
        return unitAnyM(transformerStream().map(s -> s.intersperse(value)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#reverse()
     */
    @Override
    default Traversable<T> reverse() {
        return unitAnyM(transformerStream().map(s -> s.reverse()));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#shuffle()
     */
    @Override
    default Traversable<T> shuffle() {
        return unitAnyM(transformerStream().map(s -> s.shuffle()));
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#skipLast(int)
     */
    @Override
    default Traversable<T> skipLast(final int num) {
        return unitAnyM(transformerStream().map(s -> s.skipLast(num)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#limitLast(int)
     */
    @Override
    default Traversable<T> limitLast(final int num) {
        return unitAnyM(transformerStream().map(s -> s.limitLast(num)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default Traversable<T> onEmpty(final T value) {
        return unitAnyM(transformerStream().map(s -> s.onEmpty(value)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default Traversable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return unitAnyM(transformerStream().map(s -> s.onEmptyGet(supplier)));
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#shuffle(java.util.Random)
     */
    @Override
    default Traversable<T> shuffle(final Random random) {
        return unitAnyM(transformerStream().map(s -> s.shuffle(random)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#slice(long, long)
     */
    @Override
    default Traversable<T> slice(final long from, final long to) {
        return unitAnyM(transformerStream().map(s -> s.slice(from, to)));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> Traversable<T> sorted(final Function<? super T, ? extends U> function) {
        return unitAnyM(transformerStream().map(s -> s.sorted(function)));
    }

    @Override
    default ReactiveSeq<T> stream() {
        return Traversable.super.stream();
    }

}
