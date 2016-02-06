package com.aol.cyclops.collections.extensions.standard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.QuintFunction;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.functions.currying.CurryVariance;
import com.aol.cyclops.lambda.applicative.Applicative;
import com.aol.cyclops.lambda.applicative.Applicative2;
import com.aol.cyclops.lambda.applicative.Applicative3;
import com.aol.cyclops.lambda.applicative.Applicative4;
import com.aol.cyclops.lambda.applicative.Applicative5;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative2;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative3;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative4;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative5;
import com.aol.cyclops.lambda.monads.IterableFunctor;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;

public interface ListX<T> extends List<T>, MutableCollectionX<T>, MutableSequenceX<T>, Comparable<T>,IterableFunctor<T>,ZippingApplicativable<T> {
	
	public static class ZippingApplicatives {
		/**
		 * Apply a function within the maybe context e.g. 
		 * 
		 * <pre>
		 * {@code 
		    
			Maybe<Integer> m = applicative((Integer i)->i+2).ap(Maybe.of(3));
		 * }
		 * </pre>
		 * @param fn
		 * @return
		 */
		public static <T,R> ZippingApplicative<T,R,ListX<R>> applicative(Function<? super T,? extends R> fn){
			
			return ()->ListX.of(fn);
		}
		public static <T,T2,R> ZippingApplicative2<T,T2,R,ListX<R>> applicative(BiFunction<? super T,? super T2,? extends R> fn){
			return ()->ListX.of(CurryVariance.curry2(fn));
		}
		public static <T,T2,T3,R> ZippingApplicative3<T,T2,T3,R,ListX<R>> applicative(TriFunction<? super T,? super T2,? super T3,? extends R> fn){
			return ()->ListX.of(CurryVariance.curry3(fn));
		}
		public static <T,T2,T3,T4,R> ZippingApplicative4<T,T2,T3,T4,R,ListX<R>> applicative(QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
			return ()->ListX.of(CurryVariance.curry4(fn));
		}
		public static <T,T2,T3,T4,T5,R> ZippingApplicative5<T,T2,T3,T4,T5,R,ListX<R>> applicative(QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
			return ()->ListX.of(CurryVariance.curry5(fn));
		}
	}
	
	@Override
	default <R> ListX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (ListX<R>)ZippingApplicativable.super.ap1(ap);
	}
	
	static <T> Collector<T,?,List<T>> defaultCollector(){
		return Collectors.toCollection(()-> new ArrayList<>());
	}
	static <T> Collector<T,?,List<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (List<T> d)->Collections.unmodifiableList(d));

	}
	static <T> Collector<T,?,ListX<T>> toListX(){
		return Collectors.collectingAndThen(defaultCollector(), (List<T> d)->new ListXImpl<>(d,defaultCollector()));
		
	}
	public static <T> ListX<T> empty(){
		return fromIterable((List<T>) defaultCollector().supplier().get());
	}
	public static <T> ListX<T> of(T...values){
		List<T> res = (List<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> ListX<T> singleton(T value){
		return of(value);
	}
	public static <T> ListX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> ListX<T> fromIterable(Collector<T,?,List<T>>  collector,Iterable<T> it){
		if(it instanceof ListX)
			return (ListX)it;
		if(it instanceof List)
			return new ListXImpl<T>( (List)it, collector);
		return new ListXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	@Override
	default<R> ListX<R> unit(Collection<R> col){
		return fromIterable(col);
	}
	@Override
	default <R> ListX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default <R> ListX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	
	
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	public <T> Collector<T,?,List<T>> getCollector();
	
	default <T1> ListX<T1> from(Collection<T1> c){
		return ListX.<T1>fromIterable(getCollector(),c);
	}
	
	default <X> ListX<X> fromStream(Stream<X> stream){
		return new ListXImpl<>(stream.collect(getCollector()),getCollector());
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default ListX<T> reverse() {
		
		return (ListX)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> filter(Predicate<? super T> pred) {
		
		return (ListX)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (ListX)MutableCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
	
		return (ListX)MutableCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default ListX<T> limit(long num) {
		
		return (ListX)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default ListX<T> skip(long num) {
		
		return (ListX)MutableCollectionX.super.skip(num);
	}
	default  ListX<T> takeRight(int num){
		return (ListX)MutableCollectionX.super.takeRight(num);
	}
	default  ListX<T> dropRight(int num){
		return  (ListX)MutableCollectionX.super.dropRight(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> takeWhile(Predicate<? super T> p) {
		
		return (ListX)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> dropWhile(Predicate<? super T> p) {
		
		return (ListX)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> takeUntil(Predicate<? super T> p) {
		
		return (ListX)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> dropUntil(Predicate<? super T> p) {
		return (ListX)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (ListX)MutableCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default ListX<T> slice(long from, long to) {
		return (ListX)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> ListX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (ListX)MutableCollectionX.super.sorted(function);
	}
	
	default ListX<ListX<T>> grouped(int groupSize){
		return (ListX<ListX<T>>)MutableCollectionX.super.grouped(groupSize); 
	}
	default <K, A, D> ListX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return (ListX)MutableCollectionX.super.grouped(classifier,downstream);
	}
	default <K> ListX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return (ListX)MutableCollectionX.super.grouped(classifier);	 
	}
	default <U> ListX<Tuple2<T, U>> zip(Iterable<U> other){
		return (ListX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}
	default ListX<ListX<T>> sliding(int windowSize){
		return (ListX<ListX<T>>)MutableCollectionX.super.sliding(windowSize); 
	}
	default ListX<ListX<T>> sliding(int windowSize, int increment){
		return (ListX<ListX<T>>)MutableCollectionX.super.sliding(windowSize,increment); 
	}
	default ListX<T> scanLeft(Monoid<T> monoid){
		return (ListX<T>)MutableCollectionX.super.scanLeft(monoid); 
	}
	default <U> ListX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return (ListX<U>)MutableCollectionX.super.scanLeft(seed,function); 	
	}
	default ListX<T> scanRight(Monoid<T> monoid){
		return (ListX<T>)MutableCollectionX.super.scanRight(monoid); 
	}
	default <U> ListX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return (ListX<U>)MutableCollectionX.super.scanRight(identity,combiner); 
	}
	
	/* Makes a defensive copy of this ListX replacing the value at i with the specified element
	 *  (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
	 */
	default ListX<T> with(int i,T element){
		return from(stream().deleteBetween(i, i+1).insertAt(i,element).collect(getCollector()));
	}
	public ListX<T> subList(int start, int end);
	default ListX<T> plus(T e){
		add(e);
		return this;
	}
	
	default ListX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	default ListX<T> minus(int pos){
		remove(pos);
		return this;
	}
	
	default ListX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default ListX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
	default ListX<T> plus(int i, T e){
		add(i,e);
		return this;
	}
	
	
	default ListX<T> plusAll(int i, Collection<? extends T> list){
		addAll(i,list);
		return this;
	}

	@Override
	int size();

	
}
