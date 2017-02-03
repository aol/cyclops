package com.aol.cyclops2.data.collections.extensions.persistent;

import cyclops.Streams;
import cyclops.collections.immutable.PVectorX;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import cyclops.collections.ListX;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface PersistentCollectionX<T> extends FluentCollectionX<T> {
    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default PersistentCollectionX<T> plusInOrder(final T e) {
        return (PersistentCollectionX<T>) FluentCollectionX.super.plusInOrder(e);
    }

    @Override
    public <R> PersistentCollectionX<R> unit(Collection<R> col);

    <R> PersistentCollectionX<R> emptyUnit();

    <T> Reducer<? extends Collection<T>> monoid();

    @Override
    <T1> PersistentCollectionX<T1> from(Collection<T1> c);

    @Override
    default CollectionX<T> reverse() {
        return from(this.<T> monoid()
                        .mapReduce(stream().reverse()));
    }

    @Override
    default PersistentCollectionX<T> filter(final Predicate<? super T> pred) {
        FluentCollectionX<T> mapped = emptyUnit();
        final Iterator<T> it = iterator();
        while (it.hasNext()) {
            final T value = it.next();
            if (pred.test(value))
                mapped = mapped.plusInOrder(value);
        }
        return unit(mapped);

    }

    @Override
    default <R> PersistentCollectionX<R> map(final Function<? super T, ? extends R> mapper) {
        FluentCollectionX<R> mapped = emptyUnit();
        final Iterator<T> it = iterator();
        while (it.hasNext()) {
            final T next = it.next();
            mapped = mapped.plusInOrder(mapper.apply(next));
        }
        return unit(mapped);
    }

    @Override
    default <R> PersistentCollectionX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return from(this.<R> monoid()
                        .mapReduce(stream().flatMap(mapper.andThen(Streams::stream))));
    }

    @Override
    default PersistentCollectionX<T> limit(final long num) {
        PersistentCollectionX<T> mapped = emptyUnit();
        final Iterator<T> it = iterator();
        for (long i = 0; i < num && it.hasNext(); i++) {
            mapped = mapped.plusInOrder(it.next());
        }
        return mapped;

    }

    @Override
    default PersistentCollectionX<T> skip(final long num) {
        PersistentCollectionX<T> mapped = emptyUnit();
        final Iterator<T> it = iterator();
        for (long i = 0; i < num && it.hasNext(); i++) {
            it.next();
        }
        while (it.hasNext())
            mapped = mapped.plusInOrder(it.next());
        return mapped;
        //	return from(this.<T>monoid().mapReduce(reactiveStream().skip(num)));
    }

    @Override
    default PersistentCollectionX<T> dropRight(final int num) {
        return from(this.<T> monoid()
                        .mapReduce(stream().skipLast(num)));
    }

    @Override
    default PersistentCollectionX<T> takeRight(final int num) {
        return from(this.<T> monoid()
                        .mapReduce(stream().limitLast(num)));
    }

    @Override
    default PersistentCollectionX<T> takeWhile(final Predicate<? super T> p) {
        return from(this.<T> monoid()
                        .mapReduce(stream().limitWhile(p)));
    }

    @Override
    default PersistentCollectionX<T> dropWhile(final Predicate<? super T> p) {
        return from(this.<T> monoid()
                        .mapReduce(stream().skipWhile(p)));
    }

    @Override
    default PersistentCollectionX<T> takeUntil(final Predicate<? super T> p) {
        return from(this.<T> monoid()
                        .mapReduce(stream().limitUntil(p)));
    }

    @Override
    default PersistentCollectionX<T> dropUntil(final Predicate<? super T> p) {
        return from(this.<T> monoid()
                        .mapReduce(stream().skipUntil(p)));
    }

    /**
     * Performs a map operation that can call a recursive method without running out of stack space
     * <pre>
     * {@code
     * ReactiveSeq.of(10,20,30,40)
    		 .trampoline(i-> fibonacci(i))
    		 .forEach(System.out::println); 
    		 
    Trampoline<Long> fibonacci(int i){
    	return fibonacci(i,1,0);
    }
    Trampoline<Long> fibonacci(int n, long a, long b) {
       	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
    }		 
    		 
     * 55
    6765
    832040
    102334155
     * 
     * 
     * ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
    		 .trampoline(i-> fibonacci(i))
    		 .forEach(System.out::println);
    		 
    		 
     * completes successfully
     * }
     * 
    * @param mapper
    * @return
    */
    @Override
    default <R> PersistentCollectionX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return map(in -> mapper.apply(in)
                               .result());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#slice(long, long)
     */
    @Override
    default PersistentCollectionX<T> slice(final long from, final long to) {
        return from(this.<T> monoid()
                        .mapReduce(stream().slice(from, to)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> PersistentCollectionX<T> sorted(final Function<? super T, ? extends U> function) {
        return from(this.<T> monoid()
                        .mapReduce(stream().sorted(function)));
    }

    @Override
    default PersistentCollectionX<ListX<T>> grouped(final int groupSize) {
        return from(this.<ListX<T>> monoid()
                        .mapReduce(stream().grouped(groupSize)
                                           .map(ListX::fromIterable)));
    }

    @Override
    default <K, A, D> PersistentCollectionX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return from(this.<Tuple2<K, D>> monoid()
                        .mapReduce((Stream<?>)stream().grouped(classifier, downstream)));
    }

    @Override
    default <K> PersistentCollectionX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return from(this.<Tuple2<K, ReactiveSeq<T>>> monoid()
                        .mapReduce((Stream<?>)stream().grouped(classifier)));
    }

    @Override
    default <U> PersistentCollectionX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return from(this.<Tuple2<T, U>> monoid()
                        .mapReduce(stream().zip(other)));
    }

    @Override
    default <U, R> PersistentCollectionX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return from(this.<R> monoid()
                        .mapReduce(stream().zip(other, zipper)));
    }



    @Override
    default <U, R> PersistentCollectionX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return from(this.<R> monoid()
                        .mapReduce(stream().zipS(other, zipper)));
    }

    @Override
    default PersistentCollectionX<PVectorX<T>> sliding(final int windowSize) {
        return from(this.<PVectorX<T>> monoid()
                        .mapReduce(stream().sliding(windowSize)));
    }

    @Override
    default PersistentCollectionX<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return from(this.<PVectorX<T>> monoid()
                        .mapReduce(stream().sliding(windowSize, increment)));
    }

    @Override
    default PersistentCollectionX<T> scanLeft(final Monoid<T> monoid) {

        return from(this.<T> monoid()
                        .mapReduce(stream().scanLeft(monoid)));
    }

    @Override
    default PersistentCollectionX<T> scanRight(final Monoid<T> monoid) {
        return from(this.<T> monoid()
                        .mapReduce(stream().scanRight(monoid)));
    }

    @Override
    default <U> PersistentCollectionX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return from(this.<U> monoid()
                        .mapReduce(stream().scanLeft(seed, function)));
    }

    @Override
    default <U> PersistentCollectionX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return from(this.<U> monoid()
                        .mapReduce(stream().scanRight(identity, combiner)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default PersistentCollectionX<T> cycle(final long times) {

        return from(this.<T> monoid()
                        .mapReduce(stream().cycle(times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default PersistentCollectionX<T> cycle(final Monoid<T> m, final long times) {

        return from(this.<T> monoid()
                        .mapReduce(stream().cycle(m, times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> cycleWhile(final Predicate<? super T> predicate) {

        return from(this.<T> monoid()
                        .mapReduce(stream().cycleWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> cycleUntil(final Predicate<? super T> predicate) {

        return from(this.<T> monoid()
                        .mapReduce(stream().cycleUntil(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zipStream(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> PersistentCollectionX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return from(this.<Tuple2<T, U>> monoid()
                        .mapReduce(stream().zipS(other)));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> PersistentCollectionX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return from(this.<Tuple3<T, S, U>> monoid()
                        .mapReduce(stream().zip3(second, third)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> PersistentCollectionX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return from(this.<Tuple4<T, T2, T3, T4>> monoid()
                        .mapReduce(stream().zip4(second, third, fourth)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default PersistentCollectionX<Tuple2<T, Long>> zipWithIndex() {

        return from(this.<Tuple2<T, Long>> monoid()
                        .mapReduce(stream().zipWithIndex()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#distinct()
     */
    @Override
    default PersistentCollectionX<T> distinct() {

        return from(this.<T> monoid()
                        .mapReduce(stream().distinct()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted()
     */
    @Override
    default PersistentCollectionX<T> sorted() {

        return from(this.<T> monoid()
                        .mapReduce(stream().sorted()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default PersistentCollectionX<T> sorted(final Comparator<? super T> c) {

        return from(this.<T> monoid()
                        .mapReduce(stream().sorted(c)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> skipWhile(final Predicate<? super T> p) {

        return from(this.<T> monoid()
                        .mapReduce(stream().skipWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> skipUntil(final Predicate<? super T> p) {

        return from(this.<T> monoid()
                        .mapReduce(stream().skipUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> limitWhile(final Predicate<? super T> p) {

        return from(this.<T> monoid()
                        .mapReduce(stream().limitWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> limitUntil(final Predicate<? super T> p) {

        return from(this.<T> monoid()
                        .mapReduce(stream().limitUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default PersistentCollectionX<T> intersperse(final T value) {

        return from(this.<T> monoid()
                        .mapReduce(stream().intersperse(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle()
     */
    @Override
    default PersistentCollectionX<T> shuffle() {

        return from(this.<T> monoid()
                        .mapReduce(stream().shuffle()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#skipLast(int)
     */
    @Override
    default PersistentCollectionX<T> skipLast(final int num) {

        return from(this.<T> monoid()
                        .mapReduce(stream().skipLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#limitLast(int)
     */
    @Override
    default PersistentCollectionX<T> limitLast(final int num) {

        return from(this.<T> monoid()
                        .mapReduce(stream().limitLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default PersistentCollectionX<T> onEmpty(final T value) {

        return from(this.<T> monoid()
                        .mapReduce(stream().onEmpty(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PersistentCollectionX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return from(this.<T> monoid()
                        .mapReduce(stream().onEmptyGet(supplier)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PersistentCollectionX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return from(this.<T> monoid()
                        .mapReduce(stream().onEmptyThrow(supplier)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    default PersistentCollectionX<T> shuffle(final Random random) {

        return from(this.<T> monoid()
                        .mapReduce(stream().shuffle(random)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> PersistentCollectionX<U> ofType(final Class<? extends U> type) {

        return from(this.<U> monoid()
                        .mapReduce(stream().ofType(type)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default PersistentCollectionX<T> filterNot(final Predicate<? super T> fn) {

        return from(this.<T> monoid()
                        .mapReduce((Stream<?>)stream().filterNot(fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default PersistentCollectionX<T> notNull() {

        return from(this.<T> monoid()
                        .mapReduce((Stream<?>)stream().notNull()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAllS(java.util.reactiveStream.Stream)
     */
    @Override
    default PersistentCollectionX<T> removeAllS(final Stream<? extends T> stream) {

        return from(this.<T> monoid()
                        .mapReduce((Stream<?>)stream().removeAllS(stream)));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAllS(java.lang.Iterable)
     */
    @Override
    default PersistentCollectionX<T> removeAllS(final Iterable<? extends T> it) {

        return from(this.<T> monoid()
                        .mapReduce(stream().removeAllS(it)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAllS(java.lang.Object[])
     */
    @Override
    default PersistentCollectionX<T> removeAllS(final T... values) {

        return from(this.<T> monoid()
                        .mapReduce(stream().removeAllS(values)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllS(java.lang.Iterable)
     */
    @Override
    default PersistentCollectionX<T> retainAllS(final Iterable<? extends T> it) {

        return from(this.<T> monoid()
                        .mapReduce(stream().retainAllS(it)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllS(java.util.reactiveStream.Stream)
     */
    @Override
    default PersistentCollectionX<T> retainAllS(final Stream<? extends T> seq) {

        return from(this.<T> monoid()
                        .mapReduce(stream().retainAllS(seq)));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllS(java.lang.Object[])
     */
    @Override
    default PersistentCollectionX<T> retainAllS(final T... values) {

        return from(this.<T> monoid()
                        .mapReduce(stream().retainAllS(values)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> PersistentCollectionX<U> cast(final Class<? extends U> type) {

        return from(this.<U> monoid()
                        .mapReduce(stream().cast(type)));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#permutations()
     */
    @Override
    default PersistentCollectionX<ReactiveSeq<T>> permutations() {

        return from(this.<ReactiveSeq<T>> monoid()
                        .mapReduce(stream().permutations()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#combinations(int)
     */
    @Override
    default PersistentCollectionX<ReactiveSeq<T>> combinations(final int size) {
        return from(this.<ReactiveSeq<T>> monoid()
                        .mapReduce(stream().combinations(size)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.ExtendedTraversable#combinations()
     */
    @Override
    default PersistentCollectionX<ReactiveSeq<T>> combinations() {
        return from(this.<ReactiveSeq<T>> monoid()
                        .mapReduce(stream().combinations()));
    }

    @Override
    default <C extends Collection<? super T>> PersistentCollectionX<C> grouped(final int size, final Supplier<C> supplier) {

        return from(this.<C> monoid()
                        .mapReduce(stream().grouped(size, supplier)));
    }

    @Override
    default PersistentCollectionX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return from(this.<ListX<T>> monoid()
                        .mapReduce(stream().groupedUntil(predicate)));
    }

    @Override
    default PersistentCollectionX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return from(this.<ListX<T>> monoid()
                        .mapReduce(stream().groupedStatefullyUntil(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default PersistentCollectionX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return from(this.<T> monoid()
                        .mapReduce(stream().combine(predicate, op)));
    }

    @Override
    default PersistentCollectionX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return from(this.<ListX<T>> monoid()
                        .mapReduce(stream().groupedWhile(predicate)));
    }

    @Override
    default <C extends Collection<? super T>> PersistentCollectionX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return from(this.<C> monoid()
                        .mapReduce(stream().groupedWhile(predicate, factory)));
    }

    @Override
    default <C extends Collection<? super T>> PersistentCollectionX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return from(this.<C> monoid()
                        .mapReduce(stream().groupedUntil(predicate, factory)));
    }
    @Override
    default <R> PersistentCollectionX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return from(this.<R>monoid().mapReduce(stream().flatMap(fn)));
    }

    @Override
    default <R> PersistentCollectionX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return from(this.<R>monoid().mapReduce(stream().flatMapP(fn)));
    }

    @Override
    default PersistentCollectionX<T> prependS(Stream<? extends T> stream){
        return from(this.<T>monoid().mapReduce(stream().prependS(stream)));
    }

    @Override
    default PersistentCollectionX<T> append(T... values){
        return from(this.<T>monoid().mapReduce(stream().append(values)));
    }

    @Override
    default PersistentCollectionX<T> append(T value){
        return from(this.<T>monoid().mapReduce(stream().append(value)));
    }

    @Override
    default PersistentCollectionX<T> prepend(T value){
        return from(this.<T>monoid().mapReduce(stream().prepend(value)));
    }

    @Override
    default PersistentCollectionX<T> prepend(T... values){
        return from(this.<T>monoid().mapReduce(stream().prepend(values)));
    }

    @Override
    default PersistentCollectionX<T> insertAt(int pos,T... values){
        return from(this.<T>monoid().mapReduce(stream().insertAt(pos,values)));
    }

    @Override
    default PersistentCollectionX<T> deleteBetween(int start, int end){
        return from(this.<T>monoid().mapReduce(stream().deleteBetween(start,end)));
    }

    @Override
    default PersistentCollectionX<T> insertAtS(int pos, Stream<T> stream){
        return from(this.<T>monoid().mapReduce(stream().insertAtS(pos,stream)));

    }

}
