package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.matcher2.Case;
import com.aol.cyclops.matcher2.CheckValues;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicative;

public interface QueueX<T> extends Queue<T>,  MutableCollectionX<T> {
	
	static <T> Collector<T,?,Queue<T>> defaultCollector(){
		return Collectors.toCollection(()-> new LinkedList<>());
	}
	
	public static <T> QueueX<T> empty(){
		return fromIterable((Queue<T>) defaultCollector().supplier().get());
	}
	@SafeVarargs
	public static <T> QueueX<T> of(T...values){
		Queue<T> res = (Queue<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> QueueX<T> singleton(T value){
		return QueueX.<T>of(value);
	}
	public static <T> QueueX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> QueueX<T> fromIterable(Collector<T,?,Queue<T>>  collector,Iterable<T> it){
		if(it instanceof QueueX)
			return (QueueX)it;
		if(it instanceof Deque)
			return new QueueXImpl<T>( (Queue)it, collector);
		return new QueueXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	
	public <T> Collector<T,?,Queue<T>> getCollector();
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default QueueX<T> toQueueX() {
		return this;
	}
	
	default <T1> QueueX<T1> from(Collection<T1> c){
		return QueueX.<T1>fromIterable(getCollector(),c);
	}
	
	default <X> QueueX<X> fromStream(Stream<X> stream){
		return new QueueXImpl<>(stream.collect(getCollector()),getCollector());
	}
	@Override
	default <R> QueueX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (QueueX<R>)MutableCollectionX.super.ap1(ap);
	}
	@Override
	default<R> QueueX<R> unit(Collection<R> col){
		return fromIterable(col);
	}
	@Override
	default <R> QueueX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default <R> QueueX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default QueueX<T> reverse() {
		
		return (QueueX<T>)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> filter(Predicate<? super T> pred) {
		
		return (QueueX<T>)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (QueueX<R>)MutableCollectionX.super.<R>map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		
		return (QueueX<R>)MutableCollectionX.super.<R>flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default QueueX<T> limit(long num) {
	
		return (QueueX<T>)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default QueueX<T> skip(long num) {
		
		return (QueueX<T>)MutableCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> takeWhile(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> dropWhile(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.dropWhile(p);
	}
	default  QueueX<T> takeRight(int num){
		return (QueueX<T>)MutableCollectionX.super.takeRight(num);
	}
	default  QueueX<T> dropRight(int num){
		return  (QueueX<T>)MutableCollectionX.super.dropRight(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> takeUntil(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> dropUntil(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (QueueX<R>)MutableCollectionX.super.<R>trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default QueueX<T> slice(long from, long to) {
		
		return (QueueX<T>)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> QueueX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (QueueX<T>)MutableCollectionX.super.sorted(function);
	}
	
	default QueueX<ListX<T>> grouped(int groupSize){
		return (QueueX<ListX<T>>)MutableCollectionX.super.grouped(groupSize); 
	}
	default <K, A, D> QueueX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return (QueueX)MutableCollectionX.super.grouped(classifier,downstream);
	}
	default <K> QueueX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return (QueueX)MutableCollectionX.super.grouped(classifier);	 
	}
	default <U> QueueX<Tuple2<T, U>> zip(Iterable<U> other){
		return (QueueX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> QueueX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (QueueX<R>)MutableCollectionX.super.zip(other, zipper);
	}

	default QueueX<ListX<T>> sliding(int windowSize){
		return (QueueX<ListX<T>>)MutableCollectionX.super.sliding(windowSize); 
	}
	default QueueX<ListX<T>> sliding(int windowSize, int increment){
		return (QueueX<ListX<T>>)MutableCollectionX.super.sliding(windowSize,increment); 
	}
	default QueueX<T> scanLeft(Monoid<T> monoid){
		return (QueueX<T>)MutableCollectionX.super.scanLeft(monoid); 
	}
	default <U> QueueX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return (QueueX<U>)MutableCollectionX.super.scanLeft(seed,function); 	
	}
	default QueueX<T> scanRight(Monoid<T> monoid){
		return (QueueX<T>)MutableCollectionX.super.scanRight(monoid); 
	}
	default <U> QueueX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return (QueueX<U>)MutableCollectionX.super.scanRight(identity,combiner); 
	}
	
	default QueueX<T> plus(T e){
		add(e);
		return this;
	}
	
	default QueueX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	default QueueX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default QueueX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#peek(java.util.function.Consumer)
	 */
	@Override
	default QueueX<T> peek(Consumer<? super T> c) {
		
		return (QueueX<T>)MutableCollectionX.super.peek(c);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		return (QueueX<R>)MutableCollectionX.super.patternMatch(defaultValue, case1);
	}

	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> QueueX<U> cast(Class<U> type) {
		
		return (QueueX<U>)MutableCollectionX.super.cast(type);
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(int)
	 */
	@Override
	default  QueueX<T> cycle(int times) {
		
		return (QueueX<T>)MutableCollectionX.super.cycle(times);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default  QueueX<T> cycle(Monoid<T> m, int times) {
		
		return (QueueX<T>)MutableCollectionX.super.cycle(m, times);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (QueueX<T>)MutableCollectionX.super.cycleWhile(predicate);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (QueueX<T>)MutableCollectionX.super.cycleUntil(predicate);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U>  QueueX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (QueueX<Tuple2<T, U>>)MutableCollectionX.super.zipStream(other);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U>  QueueX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (QueueX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U>  QueueX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (QueueX)MutableCollectionX.super.zip3(second, third);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4>  QueueX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (QueueX<Tuple4<T, T2, T3, T4>>)MutableCollectionX.super.zip4(second, third, fourth);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipWithIndex()
	 */
	@Override
	default  QueueX<Tuple2<T, Long>> zipWithIndex() {
		
		return (QueueX<Tuple2<T, Long>>)MutableCollectionX.super.zipWithIndex();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#distinct()
	 */
	@Override
	default  QueueX<T> distinct() {
		
		return (QueueX<T>)MutableCollectionX.super.distinct();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted()
	 */
	@Override
	default  QueueX<T> sorted() {
		
		return (QueueX<T>)MutableCollectionX.super.sorted();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default  QueueX<T> sorted(Comparator<? super T> c) {
		
		return (QueueX<T>)MutableCollectionX.super.sorted(c);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> skipWhile(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.skipWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> skipUntil(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.skipUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> limitWhile(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.limitWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> limitUntil(Predicate<? super T> p) {
		
		return (QueueX<T>)MutableCollectionX.super.limitUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default  QueueX<T> intersperse(T value) {
		
		return (QueueX<T>)MutableCollectionX.super.intersperse(value);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle()
	 */
	@Override
	default  QueueX<T> shuffle() {
		
		return (QueueX<T>)MutableCollectionX.super.shuffle();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipLast(int)
	 */
	@Override
	default  QueueX<T> skipLast(int num) {
		
		return (QueueX<T>)MutableCollectionX.super.skipLast(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
	 */
	@Override
	default  QueueX<T> limitLast(int num) {
		
		return (QueueX<T>)MutableCollectionX.super.limitLast(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default  QueueX<T> onEmpty(T value) {
		
		return (QueueX<T>)MutableCollectionX.super.onEmpty(value);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default  QueueX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (QueueX<T>)MutableCollectionX.super.onEmptyGet(supplier);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable>  QueueX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (QueueX<T>)MutableCollectionX.super.onEmptyThrow(supplier);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default  QueueX<T> shuffle(Random random) {
		
		return (QueueX<T>)MutableCollectionX.super.shuffle(random);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U>  QueueX<U> ofType(Class<U> type) {
		
		return (QueueX<U>)MutableCollectionX.super.ofType(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default  QueueX<T> filterNot(Predicate<? super T> fn) {
		
		return (QueueX<T>)MutableCollectionX.super.filterNot(fn);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
	 */
	@Override
	default  QueueX<T> notNull() {
		
		return (QueueX<T>)MutableCollectionX.super.notNull();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default  QueueX<T> removeAll(Stream<T> stream) {
		
		return (QueueX<T>)MutableCollectionX.super.removeAll(stream);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default  QueueX<T> removeAll(Iterable<T> it) {
		
		return (QueueX<T>)MutableCollectionX.super.removeAll(it);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default  QueueX<T> removeAll(T... values) {
		
		return (QueueX<T>)MutableCollectionX.super.removeAll(values);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default  QueueX<T> retainAll(Iterable<T> it) {
		
		return (QueueX<T>)MutableCollectionX.super.retainAll(it);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default  QueueX<T> retainAll(Stream<T> stream) {
		
		return (QueueX<T>)MutableCollectionX.super.retainAll(stream);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default  QueueX<T> retainAll(T... values) {
		
		return (QueueX<T>)MutableCollectionX.super.retainAll(values);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default  QueueX<T> retainMatches(Matcher<T> m) {
		
		return (QueueX<T>)MutableCollectionX.super.retainMatches(m);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default QueueX<T> removeMatches(Matcher<T> m) {
		
		return (QueueX<T>)MutableCollectionX.super.removeMatches(m);
	}
	
	
}
