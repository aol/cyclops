package com.oath.cyclops.data.collections.extensions.standard;

import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import com.oath.cyclops.types.persistent.PersistentCollection;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.companion.Streams;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import org.reactivestreams.Publisher;

public interface LazyCollectionX<T> extends FluentCollectionX<T> {



    //Add to each collection type : also check AnyMSeq
    @Override
    default <R> LazyCollectionX<R> mergeMap(int maxConcurency, Function<? super T, ? extends Publisher<? extends R>> fn){
        return fromStream(stream().mergeMap(maxConcurency,fn));
    }

    @Override
    default <R> LazyCollectionX<R> retry(final Function<? super T, ? extends R> fn) {
        return fromStream(stream().retry(fn));
    }

    @Override
    default LazyCollectionX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return fromStream(stream().combine(op,predicate));
    }

    @Override
    default <R> LazyCollectionX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return fromStream(stream().retry(fn,retries,delay,timeUnit));
    }

    @Override
    default LazyCollectionX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return fromStream(stream().recover(fn));
    }

    @Override
    default <EX extends Throwable> LazyCollectionX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return fromStream(stream().recover(exceptionClass,fn));
    }

  @Override
    default <T2, R> LazyCollectionX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return fromStream(stream().zip(fn, publisher));
    }

    @Override
    default <U> LazyCollectionX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return fromStream(stream().zipWithPublisher(other));
    }

    @Override
    default <S, U, R> LazyCollectionX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return fromStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> LazyCollectionX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return fromStream(stream().zip4(second,third,fourth,fn));
    }




    /**
     * @param stream Create a MultableCollectionX from a Stream
     * @return LazyCollectionX
     */
    <X> LazyCollectionX<X> fromStream(ReactiveSeq<X> stream);

    /* (non-Javadoc)
     * @see CollectionX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default LazyCollectionX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return fromStream(stream().combine(predicate, op));
    }

    /* (non-Javadoc)
     * @see CollectionX#reverse()
     */
    @Override
    default LazyCollectionX<T> reverse() {
        return fromStream(stream().reverse());
    }


    @Override
    default LazyCollectionX<T> filter(final Predicate<? super T> pred) {
        return fromStream(stream().filter(pred));
    }


    @Override
    default <R> CollectionX<R> map(final Function<? super T, ? extends R> mapper) {
        return fromStream(stream().map(mapper));
    }


    default <R> CollectionX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return fromStream(stream().flatMap(mapper.andThen(Streams::stream)));
    }


    @Override
    default LazyCollectionX<T> take(final long num) {
        return fromStream(stream().limit(num));
    }


    @Override
    default LazyCollectionX<T> drop(final long num) {
        return fromStream(stream().skip(num));
    }






    @Override
    default LazyCollectionX<T> slice(final long from, final long to) {
        return fromStream(stream().slice(from, to));
    }


    @Override
    default LazyCollectionX<Vector<T>> grouped(final int groupSize) {
        return fromStream(stream().grouped(groupSize));
    }




    /* (non-Javadoc)
     * @see CollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> LazyCollectionX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return fromStream(stream().zip(other));
    }

    /* (non-Javadoc)
     * @see CollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> LazyCollectionX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return fromStream(stream().zip(other, zipper));
    }



    @Override
    default LazyCollectionX<Seq<T>> sliding(final int windowSize) {
        return fromStream(stream().sliding(windowSize));
    }


    @Override
    default LazyCollectionX<Seq<T>> sliding(final int windowSize, final int increment) {
        return fromStream(stream().sliding(windowSize, increment));
    }


    @Override
    default LazyCollectionX<T> scanLeft(final Monoid<T> monoid) {
        return fromStream(stream().scanLeft(monoid));
    }


    @Override
    default <U> LazyCollectionX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return fromStream(stream().scanLeft(seed, function));
    }


    @Override
    default LazyCollectionX<T> scanRight(final Monoid<T> monoid) {
        return fromStream(stream().scanRight(monoid));
    }


    @Override
    default <U> LazyCollectionX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return fromStream(stream().scanRight(identity, combiner));
    }


    @Override
    default <U extends Comparable<? super U>> LazyCollectionX<T> sorted(final Function<? super T, ? extends U> function) {
        return fromStream(stream().sorted(function));
    }


    @Override
    default LazyCollectionX<T> plus(final T e) {
        add(e);
        return this;
    }


    @Override
    default LazyCollectionX<T> plusAll(final Iterable<? extends T> list) {
        addAll(ListX.fromIterable(list));
        return this;
    }


    @Override
    default LazyCollectionX<T> removeValue(final T e) {
        removeValue(e);
        return this;
    }


    @Override
    default LazyCollectionX<T> removeAll(final Iterable<? extends T> list) {
        removeAll((Iterable)ListX.fromIterable(list));
        return this;
    }


    @Override
    default LazyCollectionX<T> cycle(final long times) {

        return fromStream(stream().cycle(times));
    }


    @Override
    default LazyCollectionX<T> cycle(final Monoid<T> m, final long times) {

        return fromStream(stream().cycle(m, times));
    }


    @Override
    default LazyCollectionX<T> cycleWhile(final Predicate<? super T> predicate) {

        return fromStream(stream().cycleWhile(predicate));
    }


    @Override
    default LazyCollectionX<T> cycleUntil(final Predicate<? super T> predicate) {

        return fromStream(stream().cycleUntil(predicate));
    }


    @Override
    default <U> LazyCollectionX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return fromStream(stream().zipWithStream(other));
    }



    @Override
    default <S, U> LazyCollectionX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return fromStream(stream().zip3(second, third));
    }


    @Override
    default <T2, T3, T4> LazyCollectionX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                     final Iterable<? extends T4> fourth) {

        return fromStream(stream().zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see CollectionX#zipWithIndex()
     */
    @Override
    default LazyCollectionX<Tuple2<T, Long>> zipWithIndex() {

        return fromStream(stream().zipWithIndex());
    }

    /* (non-Javadoc)
     * @see CollectionX#distinct()
     */
    @Override
    default LazyCollectionX<T> distinct() {

        return fromStream(stream().distinct());
    }

    /* (non-Javadoc)
     * @see CollectionX#sorted()
     */
    @Override
    default LazyCollectionX<T> sorted() {

        return fromStream(stream().sorted());
    }

    /* (non-Javadoc)
     * @see CollectionX#sorted(java.util.Comparator)
     */
    @Override
    default LazyCollectionX<T> sorted(final Comparator<? super T> c) {

        return fromStream(stream().sorted(c));
    }


    default LazyCollectionX<T> dropWhile(final Predicate<? super T> p) {

        return fromStream(stream().dropWhile(p));
    }

    default LazyCollectionX<T> dropUntil(final Predicate<? super T> p) {

        return fromStream(stream().dropUntil(p));
    }


    default LazyCollectionX<T> takeWhile(final Predicate<? super T> p) {

        return fromStream(stream().takeWhile(p));
    }


    default LazyCollectionX<T> takeUntil(final Predicate<? super T> p) {

        return fromStream(stream().takeUntil(p));
    }

    /* (non-Javadoc)
     * @see CollectionX#intersperse(java.lang.Object)
     */
    @Override
    default LazyCollectionX<T> intersperse(final T value) {

        return fromStream(stream().intersperse(value));
    }

    /* (non-Javadoc)
     * @see CollectionX#shuffle()
     */
    @Override
    default LazyCollectionX<T> shuffle() {

        return fromStream(stream().shuffle());
    }

    @Override
    default LazyCollectionX<T> dropRight(final int num) {

        return fromStream(stream().dropRight(num));
    }

    @Override
    default LazyCollectionX<T> takeRight(final int num) {

        return fromStream(stream().takeRight(num));
    }


    @Override
    default LazyCollectionX<T> onEmpty(final T value) {
        return fromStream(stream().onEmpty(value));
    }


    @Override
    default LazyCollectionX<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return fromStream(stream().onEmptyGet(supplier));
    }


    @Override
    default <X extends Throwable> LazyCollectionX<T> onEmptyError(final Supplier<? extends X> supplier) {
        return fromStream(stream().onEmptyError(supplier));
    }

    /* (non-Javadoc)
     * @see CollectionX#shuffle(java.util.Random)
     */
    @Override
    default LazyCollectionX<T> shuffle(final Random random) {
        return fromStream(stream().shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> LazyCollectionX<U> ofType(final Class<? extends U> type) {

        return (LazyCollectionX) FluentCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see CollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default LazyCollectionX<T> filterNot(final Predicate<? super T> fn) {
        return fromStream(stream().filterNot(fn));

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#notNull()
     */
    /* (non-Javadoc)
     * @see CollectionX#notNull()
     */
    @Override
    default LazyCollectionX<T> notNull() {
        return fromStream(stream().notNull());

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#removeAll(java.util.stream.Stream)
     */
    /* (non-Javadoc)
     * @see CollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default LazyCollectionX<T> removeStream(final Stream<? extends T> stream) {

        return fromStream(stream().removeStream(stream));
    }





    @Override
    default LazyCollectionX<T> removeAll(final T... values) {
        return fromStream(stream().removeAll(values));

    }

    /* (non-Javadoc)
     * @see CollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default LazyCollectionX<T> retainAll(final Iterable<? extends T> it) {
        return fromStream(stream().retainAll(it));
    }

    /* (non-Javadoc)
     * @see CollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default LazyCollectionX<T> retainStream(final Stream<? extends T> stream) {
        return fromStream(stream().retainStream(stream));
    }



    /* (non-Javadoc)
     * @see CollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default LazyCollectionX<T> retainAll(final T... values) {
        return fromStream(stream().retainAll(values));
    }


    @Override
    default LazyCollectionX<ReactiveSeq<T>> permutations() {
        return fromStream(stream().permutations());

    }


    @Override
    default LazyCollectionX<ReactiveSeq<T>> combinations(final int size) {
        return fromStream(stream().combinations(size));
    }


    @Override
    default LazyCollectionX<ReactiveSeq<T>> combinations() {
        return fromStream(stream().combinations());
    }


    @Override
    default <C extends PersistentCollection<? super T>> LazyCollectionX<C> grouped(final int size, final Supplier<C> supplier) {

        return fromStream(stream().grouped(size, supplier));
    }


    @Override
    default LazyCollectionX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return fromStream(stream().groupedUntil(predicate));
    }


    @Override
    default LazyCollectionX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return fromStream(stream().groupedWhile(predicate));
    }


    @Override
    default <C extends PersistentCollection<? super T>> LazyCollectionX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromStream(stream().groupedWhile(predicate, factory));
    }


    @Override
    default <C extends PersistentCollection<? super T>> LazyCollectionX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return fromStream(stream().groupedUntil(predicate, factory));
    }


    @Override
    default LazyCollectionX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {
        return fromStream(stream().groupedUntil(predicate));
    }

    @Override
    default <R> LazyCollectionX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return fromStream(stream().flatMap(fn));
    }

    @Override
    default <R> LazyCollectionX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return fromStream(stream().mergeMap(fn));
    }

    @Override
    default LazyCollectionX<T> prependStream(Stream<? extends T> stream){
        return fromStream(stream().prependStream(stream));
    }

    @Override
    default LazyCollectionX<T> appendAll(T... values){
        return fromStream(stream().appendAll(values));
    }

    @Override
    default LazyCollectionX<T> append(T value){
        return fromStream(stream().append(value));
    }

    @Override
    default LazyCollectionX<T> prepend(T value){
        return fromStream(stream().prepend(value));
    }

    @Override
    default LazyCollectionX<T> prependAll(T... values){
        return fromStream(stream().prependAll(values));
    }

    @Override
    default LazyCollectionX<T> insertAt(int pos, T... values){
        return fromStream(stream().insertAt(pos,values));
    }
    @Override
    default LazyCollectionX<T> insertAt(int pos, T value){
        return fromStream(stream().insertAt(pos,value));
    }

    @Override
    default LazyCollectionX<T> deleteBetween(int start, int end){
        return fromStream(stream().deleteBetween(start,end));
    }

    @Override
    default LazyCollectionX<T> insertStreamAt(int pos, Stream<T> stream){
        return fromStream(stream().insertStreamAt(pos,stream));
    }

    @Override
    default LazyCollectionX<T> materialize() {
        return this;
    }



    @Override
    default LazyCollectionX<T> removeAt(long pos) {
        return fromStream(stream().removeAt(pos));
    }

    @Override
    default LazyCollectionX<T> removeFirst(Predicate<? super T> pred) {
        return fromStream(stream().removeFirst(pred));
    }

    @Override
    default LazyCollectionX<T> appendAll(Iterable<? extends T> value) {
        return fromStream(stream().appendAll(value));
    }

    @Override
    default LazyCollectionX<T> prependAll(Iterable<? extends T> value) {
        return fromStream(stream().prependAll(value));
    }


    @Override
    default LazyCollectionX<T> updateAt(int pos, T value) {
        return fromStream(stream().updateAt(pos,value));
    }



    @Override
    default LazyCollectionX<T> insertAt(int pos, Iterable<? extends T> values) {
        return fromStream(stream().insertAt(pos,values));
    }

    @Override
    default LazyCollectionX<T> insertAt(int pos, ReactiveSeq<? extends T> values) {
        return fromStream(stream().insertAt(pos,values));
    }
}
