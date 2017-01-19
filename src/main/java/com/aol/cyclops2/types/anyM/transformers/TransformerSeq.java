package com.aol.cyclops2.types.anyM.transformers;

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

import com.aol.cyclops2.types.*;
import cyclops.collections.DequeX;
import cyclops.collections.QueueX;
import cyclops.collections.SetX;
import cyclops.collections.immutable.*;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import cyclops.collections.ListX;
import cyclops.monads.WitnessType;
import com.aol.cyclops2.types.stream.ConvertableSequence;
import com.aol.cyclops2.types.stream.ToStream;

public interface TransformerSeq<W extends WitnessType<W>,T> extends Unwrapable,
                                                                    ConvertableSequence<T>,
                                                                    Traversable<T>,
                                                                    ToStream<T>,
                                                                    Publisher<T> {


    @Override
    default DequeX<T> toDequeX() {
        return ConvertableSequence.super.toDequeX().materialize();
    }

    @Override
    default QueueX<T> toQueueX() {
        return ConvertableSequence.super.toQueueX().materialize();
    }

    @Override
    default SetX<T> toSetX() {
        return ConvertableSequence.super.toSetX().materialize();
    }

    @Override
    default ListX<T> toListX() {
        return ConvertableSequence.super.toListX().materialize();
    }

    @Override
    default PStackX<T> toPStackX() {
        return ConvertableSequence.super.toPStackX().materialize();
    }

    @Override
    default PVectorX<T> toPVectorX() {
        return ConvertableSequence.super.toPVectorX().materialize();
    }

    @Override
    default PQueueX<T> toPQueueX() {
        return ConvertableSequence.super.toPQueueX().materialize();
    }
    @Override
    default PBagX<T> toPBagX() {
        return ConvertableSequence.super.toPBagX().materialize();
    }

    @Override
    default PSetX<T> toPSetX() {
        return ConvertableSequence.super.toPSetX().materialize();
    }
    public boolean isSeqPresent();

    <T> TransformerSeq<W,T> unitAnyM(AnyM<W,Traversable<T>> traversable);

    AnyM<W,? extends FoldableTraversable<T>> transformerStream();


    @Override
    default Traversable<T> prependS(Stream<? extends T> stream){
        return unitAnyM(transformerStream().map(s -> s.prependS(stream)));
    }

    @Override
    default Traversable<T> append(T... values){
        return unitAnyM(transformerStream().map(s -> s.append(values)));
    }

    @Override
    default Traversable<T> append(T value){
        return unitAnyM(transformerStream().map(s -> s.append(value)));
    }

    @Override
    default Traversable<T> prepend(T value){
        return unitAnyM(transformerStream().map(s -> s.prepend(value)));
    }

    @Override
    default Traversable<T> prepend(T... values){
        return unitAnyM(transformerStream().map(s -> s.prepend(values)));
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
    default Traversable<T> insertAtS(int pos, Stream<T> stream){
        return unitAnyM(transformerStream().map(s -> s.insertAtS(pos,stream)));
    }

    /* (non-Javadoc)
         * @see com.aol.cyclops2.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
         */
    @Override
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return unitAnyM(transformerStream().map(s -> s.combine(predicate, op)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#subscribeAll(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {

        transformerStream().forEach(n -> n.subscribe(s));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#cycle(int)
     */
    @Override
    default Traversable<T> cycle(final long times) {
        return unitAnyM(transformerStream().map(s -> s.cycle(times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#cycle(cyclops2.function.Monoid, int)
     */
    @Override
    default Traversable<T> cycle(final Monoid<T> m, final long times) {
        return unitAnyM(transformerStream().map(s -> s.cycle(m, times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleWhile(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.cycleWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleUntil(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.cycleUntil(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        final AnyM<W,Traversable<R>> zipped = transformerStream().map(s -> s.zip(other, zipper));
        return unitAnyM(zipped);

    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip((Iterable<? extends U>) ReactiveSeq.fromStream(other), zipper);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#zipStream(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        final Streamable<? extends U> streamable = Streamable.fromStream(other);
        return unitAnyM(transformerStream().map(s -> s.zipS(streamable.stream())));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return zipS((Stream<? extends U>) ReactiveSeq.fromIterable(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> Traversable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        final Streamable<? extends S> streamable2 = Streamable.fromIterable(second);
        final Streamable<? extends U> streamable3 = Streamable.fromIterable(third);
        final AnyM<W,Traversable<Tuple3<T, S, U>>> zipped = transformerStream().map(s -> s.zip3(streamable2.stream(), streamable3.stream()));
        return unitAnyM(zipped);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
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
     * @see com.aol.cyclops2.types.Traversable#zipWithIndex()
     */
    @Override
    default Traversable<Tuple2<T, Long>> zipWithIndex() {
        return unitAnyM(transformerStream().map(s -> s.zipWithIndex()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#sliding(int)
     */
    @Override
    default Traversable<PVectorX<T>> sliding(final int windowSize) {
        return unitAnyM(transformerStream().map(s -> s.sliding(windowSize)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#sliding(int, int)
     */
    @Override
    default Traversable<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return unitAnyM(transformerStream().map(s -> s.sliding(windowSize, increment)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> grouped(final int size, final Supplier<C> supplier) {
        return unitAnyM(transformerStream().map(s -> s.grouped(size, supplier)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedUntil(predicate)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default Traversable<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedStatefullyUntil(predicate)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s -> s.groupedWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s -> s.groupedWhile(predicate, factory)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s -> s.groupedUntil(predicate, factory)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#grouped(int)
     */
    @Override
    default Traversable<ListX<T>> grouped(final int groupSize) {
        return unitAnyM(transformerStream().map(s -> s.grouped(groupSize)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#grouped(java.util.function.Function, java.util.reactiveStream.Collector)
     */
    @Override
    default <K, A, D> Traversable<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return unitAnyM(transformerStream().map(s -> s.grouped(classifier, downstream)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> Traversable<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return unitAnyM(transformerStream().map(s -> s.grouped(classifier)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#distinct()
     */
    @Override
    default Traversable<T> distinct() {
        return unitAnyM(transformerStream().map(s -> s.distinct()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    default Traversable<T> scanLeft(final Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s -> s.scanLeft(monoid)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return unitAnyM(transformerStream().map(s -> s.scanLeft(seed, function)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#scanRight(cyclops2.function.Monoid)
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
     * @see com.aol.cyclops2.types.Traversable#sorted()
     */
    @Override
    default Traversable<T> sorted() {
        return unitAnyM(transformerStream().map(s -> s.sorted()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default Traversable<T> sorted(final Comparator<? super T> c) {
        return unitAnyM(transformerStream().map(s -> s.sorted(c)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.takeWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.dropWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.takeUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.dropUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#dropRight(int)
     */
    @Override
    default Traversable<T> dropRight(final int num) {
        return unitAnyM(transformerStream().map(s -> s.dropRight(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#takeRight(int)
     */
    @Override
    default Traversable<T> takeRight(final int num) {
        return unitAnyM(transformerStream().map(s -> s.takeRight(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#skip(long)
     */
    @Override
    default Traversable<T> skip(final long num) {
        return unitAnyM(transformerStream().map(s -> s.skip(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.skipWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.skipUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#limit(long)
     */
    @Override
    default Traversable<T> limit(final long num) {
        return unitAnyM(transformerStream().map(s -> s.limit(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitWhile(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.limitWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitUntil(final Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s -> s.limitUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default Traversable<T> intersperse(final T value) {
        return unitAnyM(transformerStream().map(s -> s.intersperse(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#reverse()
     */
    @Override
    default Traversable<T> reverse() {
        return unitAnyM(transformerStream().map(s -> s.reverse()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#shuffle()
     */
    @Override
    default Traversable<T> shuffle() {
        return unitAnyM(transformerStream().map(s -> s.shuffle()));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#skipLast(int)
     */
    @Override
    default Traversable<T> skipLast(final int num) {
        return unitAnyM(transformerStream().map(s -> s.skipLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#limitLast(int)
     */
    @Override
    default Traversable<T> limitLast(final int num) {
        return unitAnyM(transformerStream().map(s -> s.limitLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default Traversable<T> onEmpty(final T value) {
        return unitAnyM(transformerStream().map(s -> s.onEmpty(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default Traversable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return unitAnyM(transformerStream().map(s -> s.onEmptyGet(supplier)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> Traversable<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return unitAnyM(transformerStream().map(s -> s.onEmptyThrow(supplier)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    default Traversable<T> shuffle(final Random random) {
        return unitAnyM(transformerStream().map(s -> s.shuffle(random)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#slice(long, long)
     */
    @Override
    default Traversable<T> slice(final long from, final long to) {
        return unitAnyM(transformerStream().map(s -> s.slice(from, to)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Traversable#sorted(java.util.function.Function)
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
