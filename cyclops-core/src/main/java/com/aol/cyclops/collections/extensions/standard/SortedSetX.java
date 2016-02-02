package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;

public interface SortedSetX<T> extends SortedSet<T>, MutableCollectionX<T> {
	static <T> Collector<T,?,SortedSet<T>> defaultCollector(){
		return Collectors.toCollection(()-> new TreeSet<T>((Comparator)Comparator.<Comparable>naturalOrder()));
	}
	static <T> Collector<T,?,SortedSet<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (SortedSet<T> d)->Collections.unmodifiableSortedSet(d));

	}
	static <T> Collector<T,?,SortedSetX<T>> toSortedSetX(){
		return Collectors.collectingAndThen(defaultCollector(), (SortedSet<T> d)->new SortedSetXImpl<>(d,defaultCollector()));
		
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
			return (SortedSetX)it;
		if(it instanceof SortedSet)
			return new SortedSetXImpl<T>( (SortedSet)it, collector);
		return new SortedSetXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	@Override
	default <R> SortedSetX<R> unit(R value){
		return singleton(value);
	}
	
	@Override
	default<R> SortedSetX<R> emptyUnit(){
		return empty();
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
		return (SortedSetX)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> filter(Predicate<? super T> pred) {
		
		return (SortedSetX)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (SortedSetX)MutableCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		
		return (SortedSetX)MutableCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default SortedSetX<T> limit(long num) {
		return (SortedSetX)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default SortedSetX<T> skip(long num) {
		
		return (SortedSetX)MutableCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> takeWhile(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> dropWhile(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> takeUntil(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> dropUntil(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.dropUntil(p);
	}
	default  SortedSetX<T> takeRight(int num){
		return (SortedSetX)MutableCollectionX.super.takeRight(num);
	}
	default  SortedSetX<T> dropRight(int num){
		return  (SortedSetX)MutableCollectionX.super.dropRight(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (SortedSetX)MutableCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default SortedSetX<T> slice(long from, long to) {
		
		return (SortedSetX)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> SortedSetX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (SortedSetX)MutableCollectionX.super.sorted(function);
	}
	
	default SortedSetX<ListX<T>> grouped(int groupSize){
		return (SortedSetX<ListX<T>>)MutableCollectionX.super.grouped(groupSize); 
	}
	default <K, A, D> SortedSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return (SortedSetX)MutableCollectionX.super.grouped(classifier,downstream);
	}
	default <K> SortedSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return (SortedSetX)MutableCollectionX.super.grouped(classifier);	 
	}
	default <U> SortedSetX<Tuple2<T, U>> zip(Iterable<U> other){
		return (SortedSetX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}
	default SortedSetX<ListX<T>> sliding(int windowSize){
		return (SortedSetX<ListX<T>>)MutableCollectionX.super.sliding(windowSize); 
	}
	default SortedSetX<ListX<T>> sliding(int windowSize, int increment){
		return (SortedSetX<ListX<T>>)MutableCollectionX.super.sliding(windowSize,increment); 
	}
	default SortedSetX<T> scanLeft(Monoid<T> monoid){
		return (SortedSetX<T>)MutableCollectionX.super.scanLeft(monoid); 
	}
	default <U> SortedSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return (SortedSetX<U>)MutableCollectionX.super.scanLeft(seed,function); 	
	}
	default SortedSetX<T> scanRight(Monoid<T> monoid){
		return (SortedSetX<T>)MutableCollectionX.super.scanRight(monoid); 
	}
	default <U> SortedSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return (SortedSetX<U>)MutableCollectionX.super.scanRight(identity,combiner); 
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
	
}
