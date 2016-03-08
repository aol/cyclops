package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
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
import com.aol.cyclops.control.Matchable.CheckValues;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicative;
import com.aol.cyclops.util.stream.StreamUtils;

public interface SetX<T> extends Set<T>, MutableCollectionX<T> {
	static <T> Collector<T,?,Set<T>> defaultCollector(){
		return Collectors.toCollection(()-> new HashSet<>());
	}
	static <T> Collector<T,?,Set<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (Set<T> d)->Collections.unmodifiableSet(d));

	}
	
	public static <T> SetX<T> empty(){
		return fromIterable((Set<T>) defaultCollector().supplier().get());
	}
	@SafeVarargs
	public static <T> SetX<T> of(T...values){
		Set<T> res = (Set<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> SetX<T> singleton(T value){
		return SetX.<T>of(value);
	}
	public static <T> SetX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> SetX<T> fromIterable(Collector<T,?,Set<T>>  collector,Iterable<T> it){
		if(it instanceof SetX)
			return (SetX)it;
		if(it instanceof Set)
			return new SetXImpl<T>( (Set)it, collector);
		return new SetXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	
	/**
     * Combine two adjacent elements in a SetX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  SetX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced SetX
     */
    default SetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op){
        return (SetX<T>)MutableCollectionX.super.combine(predicate,op);
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default SetX<T> toSetX() {
		return this;
	}
	
	@Override
	default ReactiveSeq<T> stream(){
		
		return ReactiveSeq.fromIterable(this);
	}

	@Override
	default<R> SetX<R> unit(Collection<R> col){
		return fromIterable(col);
	}
	@Override
	default <R> SetX<R> unit(R value){
		return singleton(value);
	}
	
	@Override
	default <R> SetX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	default <T1> SetX<T1> from(Collection<T1> c){
		return SetX.<T1>fromIterable(getCollector(),c);
	}
	public <T>Collector<T,?,Set<T>> getCollector();
	
	
	
	default <X> SetX<X> fromStream(Stream<X> stream){
		return new SetXImpl<>(stream.collect(getCollector()),getCollector());
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default SetX<T> reverse() {
		return (SetX<T>)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> filter(Predicate<? super T> pred) {
		
		return (SetX<T>)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> SetX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (SetX<R>)MutableCollectionX.super.<R>map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> SetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		
		return (SetX<R>)MutableCollectionX.super.<R>flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default SetX<T> limit(long num) {
		return (SetX<T>)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default SetX<T> skip(long num) {
		
		return (SetX<T>)MutableCollectionX.super.skip(num);
	}
	default  SetX<T> takeRight(int num){
		return (SetX<T>)MutableCollectionX.super.takeRight(num);
	}
	default  SetX<T> dropRight(int num){
		return  (SetX<T>)MutableCollectionX.super.dropRight(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> takeWhile(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> dropWhile(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> takeUntil(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> dropUntil(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> SetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (SetX<R>)MutableCollectionX.super.<R>trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default SetX<T> slice(long from, long to) {
		
		return (SetX<T>)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> SetX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (SetX<T>)MutableCollectionX.super.sorted(function);
	}
	
	default SetX<ListX<T>> grouped(int groupSize){
		return (SetX<ListX<T>>)(SetX)MutableCollectionX.super.grouped(groupSize); 
	}
	default <K, A, D> SetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return (SetX)MutableCollectionX.super.grouped(classifier,downstream);
	}
	default <K> SetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return (SetX)MutableCollectionX.super.grouped(classifier);	 
	}
	default <U> SetX<Tuple2<T, U>> zip(Iterable<U> other){
		return (SetX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> SetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (SetX<R>)MutableCollectionX.super.zip(other, zipper);
	}

	default SetX<ListX<T>> sliding(int windowSize){
		return (SetX<ListX<T>>)MutableCollectionX.super.sliding(windowSize); 
	}
	default SetX<ListX<T>> sliding(int windowSize, int increment){
		return (SetX<ListX<T>>)MutableCollectionX.super.sliding(windowSize,increment); 
	}
	default SetX<T> scanLeft(Monoid<T> monoid){
		return (SetX<T>)MutableCollectionX.super.scanLeft(monoid); 
	}
	default <U> SetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return (SetX<U>)MutableCollectionX.super.scanLeft(seed,function); 	
	}
	default SetX<T> scanRight(Monoid<T> monoid){
		return (SetX<T>)MutableCollectionX.super.scanRight(monoid); 
	}
	default <U> SetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return (SetX<U>)MutableCollectionX.super.scanRight(identity,combiner); 
	}
	

	default SetX<T> plus(T e){
		add(e);
		return this;
	}
	
	default SetX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	default SetX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default SetX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(int)
	 */
	@Override
	default ListX<T> cycle(int times) {
		
		return this.stream().cycle(times).toListX();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default ListX<T> cycle(Monoid<T> m, int times) {
		
		return this.stream().cycle(m,times).toListX();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return this.stream().cycleWhile(predicate).toListX();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return this.stream().cycleUntil(predicate).toListX();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> SetX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (SetX<Tuple2<T, U>>)MutableCollectionX.super.zipStream(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> SetX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (SetX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> SetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (SetX)MutableCollectionX.super.zip3(second, third);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> SetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (SetX<Tuple4<T, T2, T3, T4>>)MutableCollectionX.super.zip4(second, third, fourth);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zipWithIndex()
	 */
	@Override
	default SetX<Tuple2<T, Long>> zipWithIndex() {
		
		return (SetX<Tuple2<T, Long>>)MutableCollectionX.super.zipWithIndex();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#distinct()
	 */
	@Override
	default SetX<T> distinct() {
		
		return (SetX<T>)MutableCollectionX.super.distinct();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted()
	 */
	@Override
	default SetX<T> sorted() {
		
		return (SetX<T>)MutableCollectionX.super.sorted();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default SetX<T> sorted(Comparator<? super T> c) {
		
		return (SetX<T>)MutableCollectionX.super.sorted(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> skipWhile(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.skipWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> skipUntil(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.skipUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> limitWhile(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.limitWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> limitUntil(Predicate<? super T> p) {
		
		return (SetX<T>)MutableCollectionX.super.limitUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default SetX<T> intersperse(T value) {
		
		return (SetX<T>)MutableCollectionX.super.intersperse(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle()
	 */
	@Override
	default SetX<T> shuffle() {
		
		return (SetX<T>)MutableCollectionX.super.shuffle();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skipLast(int)
	 */
	@Override
	default SetX<T> skipLast(int num) {
		
		return (SetX<T>)MutableCollectionX.super.skipLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
	 */
	@Override
	default SetX<T> limitLast(int num) {
		
		return (SetX<T>)MutableCollectionX.super.limitLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default SetX<T> onEmpty(T value) {
		
		return (SetX<T>)MutableCollectionX.super.onEmpty(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default SetX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (SetX<T>)MutableCollectionX.super.onEmptyGet(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> SetX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (SetX<T>)MutableCollectionX.super.onEmptyThrow(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default SetX<T> shuffle(Random random) {
		
		return (SetX<T>)MutableCollectionX.super.shuffle(random);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> SetX<U> ofType(Class<U> type) {
		
		return (SetX<U>)MutableCollectionX.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default SetX<T> filterNot(Predicate<? super T> fn) {
		
		return (SetX<T>)MutableCollectionX.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
	 */
	@Override
	default SetX<T> notNull() {
		
		return (SetX<T>)MutableCollectionX.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default SetX<T> removeAll(Stream<T> stream) {
		
		return (SetX<T>)MutableCollectionX.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default SetX<T> removeAll(Iterable<T> it) {
		
		return (SetX<T>)MutableCollectionX.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default SetX<T> removeAll(T... values) {
		
		return (SetX<T>)MutableCollectionX.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default SetX<T> retainAll(Iterable<T> it) {
		
		return (SetX<T>)MutableCollectionX.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default SetX<T> retainAll(Stream<T> stream) {
		
		return (SetX<T>)MutableCollectionX.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default SetX<T> retainAll(T... values) {
		
		return (SetX<T>)MutableCollectionX.super.retainAll(values);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> SetX<U> cast(Class<U> type) {
		
		return (SetX<U>)MutableCollectionX.super.cast(type);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
    default <R> SetX<R> patternMatch(
            Function<CheckValues<T, R>, CheckValues<T, R>> case1,Supplier<? extends R> otherwise) {
        return (SetX<R>)MutableCollectionX.super.patternMatch(case1,otherwise);
    }
	
	  @Override
	    default <C extends Collection<? super  T>> SetX<C> grouped(int size, Supplier<C> supplier) {
	        
	        return (SetX<C>)MutableCollectionX.super.grouped(size, supplier);
	    }


	    @Override
	    default SetX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
	        
	        return (SetX<ListX<T>>)MutableCollectionX.super.groupedUntil(predicate);
	    }


	    @Override
	    default SetX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
	        
	        return (SetX<ListX<T>>)MutableCollectionX.super.groupedWhile(predicate);
	    }


	    @Override
	    default <C extends Collection<? super T>> SetX<C> groupedWhile(Predicate<? super T> predicate,
	            Supplier<C> factory) {
	        
	        return (SetX<C>)MutableCollectionX.super.groupedWhile(predicate, factory);
	    }


	    @Override
	    default <C extends Collection<? super T>> SetX<C> groupedUntil(Predicate<? super T> predicate,
	            Supplier<C> factory) {
	        
	        return (SetX<C>)MutableCollectionX.super.groupedUntil(predicate, factory);
	    }


	    @Override
	    default SetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
	        
	        return (SetX<ListX<T>>)MutableCollectionX.super.groupedStatefullyWhile(predicate);
	    }
	    @Override
	    default SetX<T> removeAll(Seq<T> stream) {
	       
	        return (SetX<T>)MutableCollectionX.super.removeAll(stream);
	    }


	    @Override
	    default SetX<T> retainAll(Seq<T> stream) {
	       
	        return (SetX<T>)MutableCollectionX.super.retainAll(stream);
	    }
	    
}
