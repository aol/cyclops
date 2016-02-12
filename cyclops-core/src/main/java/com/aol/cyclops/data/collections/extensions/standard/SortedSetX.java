package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
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
import com.aol.cyclops.internal.matcher2.Case;
import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicative;
import com.aol.cyclops.util.stream.StreamUtils;

public interface SortedSetX<T> extends SortedSet<T>,MutableCollectionX<T> {
	static <T> Collector<T,?,SortedSet<T>> defaultCollector(){
		return Collectors.toCollection(()-> new TreeSet<T>((Comparator)Comparator.<Comparable>naturalOrder()));
	}
	static <T> Collector<T,?,SortedSet<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (SortedSet<T> d)->Collections.unmodifiableSortedSet(d));

	}
	
	public static <T> SortedSetX<T> empty(){
		return fromIterable((SortedSet<T>) defaultCollector().supplier().get());
	}
	public static <T> SortedSetX<T> of(T...values){
		SortedSet<T> res = (SortedSet<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> SortedSetX<T> singleton(T value){
		return of(value);
	}
	public static <T> SortedSetX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> SortedSetX<T> fromIterable(Collector<T,?,SortedSet<T>>  collector,Iterable<T> it){
		if(it instanceof SortedSetX)
			return (SortedSetX<T>)it;
		if(it instanceof SortedSet)
			return new SortedSetXImpl<T>( (SortedSet)it, collector);
		return new SortedSetXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default SortedSetX<T> toSortedSetX() {
		return this;
	}
	
	@Override
	default <R> SortedSetX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (SortedSetX<R>)MutableCollectionX.super.ap1(ap);
	}
	@Override
	default <R> SortedSetX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default<R> SortedSetX<R> unit(Collection<R> col){
		return fromIterable(col);
	}
	
	@Override
	default <R> SortedSetX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	
	default <T1> SortedSetX<T1> from(Collection<T1> c){
		return SortedSetX.<T1>fromIterable(getCollector(),c);
	}
	
	
	public <T>Collector<T,?,SortedSet<T>> getCollector();
	
	
	
	default <X> SortedSetX<X> fromStream(Stream<X> stream){
		return new SortedSetXImpl<>(stream.collect(getCollector()),getCollector());
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default SortedSetX<T> reverse() {
		return (SortedSetX<T>)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> filter(Predicate<? super T> pred) {
		
		return (SortedSetX<T>)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (SortedSetX<R>)MutableCollectionX.super.<R>map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		
		return (SortedSetX<R>)MutableCollectionX.super.<R>flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default SortedSetX<T> limit(long num) {
		return (SortedSetX<T>)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default SortedSetX<T> skip(long num) {
		
		return (SortedSetX<T>)MutableCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> takeWhile(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> dropWhile(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> takeUntil(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> dropUntil(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.dropUntil(p);
	}
	default  SortedSetX<T> takeRight(int num){
		return (SortedSetX<T>)MutableCollectionX.super.takeRight(num);
	}
	default  SortedSetX<T> dropRight(int num){
		return  (SortedSetX<T>)MutableCollectionX.super.dropRight(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (SortedSetX<R>)MutableCollectionX.super.<R>trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default SortedSetX<T> slice(long from, long to) {
		
		return (SortedSetX<T>)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> SortedSetX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (SortedSetX<T>)MutableCollectionX.super.sorted(function);
	}
	
	default SortedSetX<ListX<T>> grouped(int groupSize){
		return (SortedSetX<ListX<T>>)(SortedSetX<T>)MutableCollectionX.super.grouped(groupSize); 
	}
	default <K, A, D> SortedSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return (SortedSetX)MutableCollectionX.super.grouped(classifier,downstream);
	}
	default <K> SortedSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return (SortedSetX)MutableCollectionX.super.grouped(classifier);	 
	}
	default <U> SortedSetX<Tuple2<T, U>> zip(Iterable<U> other){
		return (SortedSetX<Tuple2<T, U>>)(SortedSetX<T>)MutableCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> SortedSetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
		return (SortedSetX<R>)MutableCollectionX.super.zip(other, zipper);
	}

	default SortedSetX<ListX<T>> sliding(int windowSize){
		return (SortedSetX<ListX<T>>)(SortedSetX<T>)MutableCollectionX.super.sliding(windowSize); 
	}
	default SortedSetX<ListX<T>> sliding(int windowSize, int increment){
		return (SortedSetX<ListX<T>>)(SortedSetX<T>)MutableCollectionX.super.sliding(windowSize,increment); 
	}
	default SortedSetX<T> scanLeft(Monoid<T> monoid){
		return (SortedSetX<T>)(SortedSetX<T>)MutableCollectionX.super.scanLeft(monoid); 
	}
	default <U> SortedSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return (SortedSetX<U>)(SortedSetX<T>)MutableCollectionX.super.scanLeft(seed,function); 	
	}
	default SortedSetX<T> scanRight(Monoid<T> monoid){
		return (SortedSetX<T>)(SortedSetX<T>)MutableCollectionX.super.scanRight(monoid); 
	}
	default <U> SortedSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return (SortedSetX<U>)(SortedSetX<T>)MutableCollectionX.super.scanRight(identity,combiner); 
	}
	
	
	
	default SortedSetX<T> plus(T e){
		add(e);
		return this;
	}
	
	default SortedSetX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	default SortedSetX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default SortedSetX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(int)
	 */
	@Override
	default SortedSetX<T> cycle(int times) {
		
		return (SortedSetX<T>)MutableCollectionX.super.cycle(times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default SortedSetX<T> cycle(Monoid<T> m, int times) {
		
		return (SortedSetX<T>)MutableCollectionX.super.cycle(m, times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (SortedSetX<T>)MutableCollectionX.super.cycleWhile(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (SortedSetX<T>)MutableCollectionX.super.cycleUntil(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> SortedSetX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (SortedSetX<Tuple2<T, U>>)MutableCollectionX.super.zipStream(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> SortedSetX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (SortedSetX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> SortedSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (SortedSetX)MutableCollectionX.super.zip3(second, third);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> SortedSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (SortedSetX<Tuple4<T, T2, T3, T4>>)MutableCollectionX.super.zip4(second, third, fourth);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipWithIndex()
	 */
	@Override
	default SortedSetX<Tuple2<T, Long>> zipWithIndex() {
		
		return (SortedSetX<Tuple2<T, Long>>)MutableCollectionX.super.zipWithIndex();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#distinct()
	 */
	@Override
	default SortedSetX<T> distinct() {
		
		return (SortedSetX<T>)MutableCollectionX.super.distinct();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted()
	 */
	@Override
	default SortedSetX<T> sorted() {
		
		return (SortedSetX<T>)MutableCollectionX.super.sorted();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default SortedSetX<T> sorted(Comparator<? super T> c) {
		
		return (SortedSetX<T>)MutableCollectionX.super.sorted(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> skipWhile(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.skipWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> skipUntil(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.skipUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> limitWhile(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.limitWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> limitUntil(Predicate<? super T> p) {
		
		return (SortedSetX<T>)MutableCollectionX.super.limitUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default SortedSetX<T> intersperse(T value) {
		
		return (SortedSetX<T>)MutableCollectionX.super.intersperse(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle()
	 */
	@Override
	default SortedSetX<T> shuffle() {
		
		return (SortedSetX<T>)MutableCollectionX.super.shuffle();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipLast(int)
	 */
	@Override
	default SortedSetX<T> skipLast(int num) {
		
		return (SortedSetX<T>)MutableCollectionX.super.skipLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
	 */
	@Override
	default SortedSetX<T> limitLast(int num) {
		
		return (SortedSetX<T>)MutableCollectionX.super.limitLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default SortedSetX<T> onEmpty(T value) {
		
		return (SortedSetX<T>)MutableCollectionX.super.onEmpty(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default SortedSetX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (SortedSetX<T>)MutableCollectionX.super.onEmptyGet(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> SortedSetX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (SortedSetX<T>)MutableCollectionX.super.onEmptyThrow(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default SortedSetX<T> shuffle(Random random) {
		
		return (SortedSetX<T>)MutableCollectionX.super.shuffle(random);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> SortedSetX<U> ofType(Class<U> type) {
		
		return (SortedSetX<U>)MutableCollectionX.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> filterNot(Predicate<? super T> fn) {
		
		return (SortedSetX<T>)MutableCollectionX.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
	 */
	@Override
	default SortedSetX<T> notNull() {
		
		return (SortedSetX<T>)MutableCollectionX.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default SortedSetX<T> removeAll(Stream<T> stream) {
		
		return (SortedSetX<T>)MutableCollectionX.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default SortedSetX<T> removeAll(Iterable<T> it) {
		
		return (SortedSetX<T>)MutableCollectionX.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default SortedSetX<T> removeAll(T... values) {
		
		return (SortedSetX<T>)MutableCollectionX.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default SortedSetX<T> retainAll(Iterable<T> it) {
		
		return (SortedSetX<T>)MutableCollectionX.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default SortedSetX<T> retainAll(Stream<T> stream) {
		
		return (SortedSetX<T>)MutableCollectionX.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default SortedSetX<T> retainAll(T... values) {
		
		return (SortedSetX<T>)MutableCollectionX.super.retainAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default SortedSetX<T> retainMatches(Matcher<T> m) {
		
		return (SortedSetX<T>)MutableCollectionX.super.retainMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default SortedSetX<T> removeMatches(Matcher<T> m) {
		
		return (SortedSetX<T>)MutableCollectionX.super.removeMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> SortedSetX<U> cast(Class<U> type) {
		
		return (SortedSetX<U>)MutableCollectionX.super.cast(type);
	}
		
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (SortedSetX<R>)MutableCollectionX.super.patternMatch(defaultValue, case1);
	}
	
	
	
	
}
