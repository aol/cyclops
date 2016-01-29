package com.aol.cyclops.collections.extensions.standard;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;

public interface DequeX<T> extends Deque<T>, MutableCollectionX<T> {
	
	static <T> Collector<T,?,Deque<T>> defaultCollector(){
		return Collectors.toCollection(()-> new ArrayDeque<>());
	}
	
	static <T> Collector<T,?,DequeX<T>> toDequeX(){
		return Collectors.collectingAndThen(defaultCollector(), (Deque<T> d)->new DequeXImpl<>(d,defaultCollector()));
		
	}
	public static <T> DequeX<T> empty(){
		return fromIterable((Deque<T>) defaultCollector().supplier().get());
	}
	public static <T> DequeX<T> of(T...values){
		Deque<T> res = (Deque<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> DequeX<T> singleton(T value){
		return of(value);
	}
	public static <T> DequeX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> DequeX<T> fromIterable(Collector<T,?,Deque<T>>  collector,Iterable<T> it){
		if(it instanceof DequeX)
			return (DequeX)it;
		if(it instanceof Deque)
			return new DequeXImpl<T>( (Deque)it, collector);
		return new DequeXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	
	public <T> Collector<T,?,Deque<T>> getCollector();
	
	default <T1> DequeX<T1> from(Collection<T1> c){
		return DequeX.<T1>fromIterable(getCollector(),c);
	}
	
	default <X> DequeX<X> fromStream(Stream<X> stream){
		return new DequeXImpl<>(stream.collect(getCollector()),getCollector());
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default DequeX<T> reverse() {
		
		return (DequeX)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default DequeX<T> filter(Predicate<? super T> pred) {
		
		return (DequeX)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> DequeX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (DequeX)MutableCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> DequeX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
	
		return (DequeX)MutableCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default DequeX<T> limit(long num) {
		
		return (DequeX)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default DequeX<T> skip(long num) {
		
		return (DequeX)MutableCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default DequeX<T> takeWhile(Predicate<? super T> p) {
		
		return (DequeX)MutableCollectionX.super.takeWhile(p);
	}
	default  DequeX<T> takeRight(int num){
		return (DequeX)MutableCollectionX.super.takeRight(num);
	}
	default  DequeX<T> dropRight(int num){
		return  (DequeX)MutableCollectionX.super.dropRight(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default DequeX<T> dropWhile(Predicate<? super T> p) {
		
		return (DequeX)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default DequeX<T> takeUntil(Predicate<? super T> p) {
		
		return (DequeX)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default DequeX<T> dropUntil(Predicate<? super T> p) {
		return (DequeX)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> DequeX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (DequeX)MutableCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default DequeX<T> slice(long from, long to) {
		return (DequeX)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> DequeX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (DequeX)MutableCollectionX.super.sorted(function);
	}
	default DequeX<T> plus(T e){
		add(e);
		return this;
	}
	
	default DequeX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	default DequeX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default DequeX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
}
