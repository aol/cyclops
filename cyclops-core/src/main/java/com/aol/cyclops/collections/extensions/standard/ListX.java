package com.aol.cyclops.collections.extensions.standard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.FluentCollectionX;
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
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.IterableFunctor;
import com.aol.cyclops.lambda.monads.Traversable;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.builders.CheckValues;
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

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.FluentCollectionX#plusInOrder(java.lang.Object)
	 */
	@Override
	default ListX<T> plusInOrder(T e) {
		
		return (ListX<T>)MutableSequenceX.super.plusInOrder(e);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#peek(java.util.function.Consumer)
	 */
	@Override
	default ListX<T> peek(Consumer<? super T> c) {
		
		return (ListX<T>)MutableCollectionX.super.peek(c);
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (ListX<R>)MutableCollectionX.super.patternMatch(defaultValue, case1);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case2) {
		
		return (ListX<R>)MutableCollectionX.super.patternMatch(defaultValue, case1, case2);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3) {
		
		return (ListX<R>)MutableCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn4) {
		
		return (ListX<R>)MutableCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3, fn4);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn4,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn5) {
		
		return (ListX<R>)MutableCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3, fn4, fn5);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycle(int)
	 */
	@Override
	default ListX<T> cycle(int times) {
		
		return (ListX<T>)MutableCollectionX.super.cycle(times);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default ListX<T> cycle(Monoid<T> m, int times) {
		
		return (ListX<T>)MutableCollectionX.super.cycle(m, times);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (ListX<T>)MutableCollectionX.super.cycleWhile(predicate);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (ListX<T>)MutableCollectionX.super.cycleUntil(predicate);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> ListX<Tuple2<T, U>> zipStream(Stream<U> other) {

		return (ListX<Tuple2<T, U>>)MutableCollectionX.super.zipStream(other);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> ListX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (ListX<Tuple2<T, U>>)MutableCollectionX.super.zip(other);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> ListX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (ListX)MutableCollectionX.super.zip3(second, third);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> ListX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (ListX)MutableCollectionX.super.zip4(second, third, fourth);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zipWithIndex()
	 */
	@Override
	default ListX<Tuple2<T, Long>> zipWithIndex() {
		
		return (ListX)MutableCollectionX.super.zipWithIndex();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#sorted()
	 */
	@Override
	default ListX<T> sorted() {
		
		return (ListX<T>)MutableCollectionX.super.sorted();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
	 */
	@Override
	default ListX<T> sorted(Comparator<? super T> c) {
		
		return (ListX<T>)MutableCollectionX.super.sorted(c);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> skipWhile(Predicate<? super T> p) {
		
		return (ListX<T>)MutableCollectionX.super.skipWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> skipUntil(Predicate<? super T> p) {
		
		return (ListX<T>)MutableCollectionX.super.skipUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#shuffle()
	 */
	@Override
	default ListX<T> shuffle() {
		
		return (ListX<T>)MutableCollectionX.super.shuffle();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipLast(int)
	 */
	@Override
	default ListX<T> skipLast(int num) {
		
		return (ListX<T>)MutableCollectionX.super.skipLast(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#shuffle(java.util.Random)
	 */
	@Override
	default ListX<T> shuffle(Random random) {
		
		return (ListX<T>)MutableCollectionX.super.shuffle(random);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#permutations()
	 */
	@Override
	default ListX<SequenceM<T>> permutations() {
		
		return (ListX<SequenceM<T>>)MutableCollectionX.super.permutations();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#combinations(int)
	 */
	@Override
	default ListX<SequenceM<T>> combinations(int size) {
		
		return (ListX<SequenceM<T>>)MutableCollectionX.super.combinations(size);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#combinations()
	 */
	@Override
	default ListX<SequenceM<T>> combinations() {
		
		return (ListX<SequenceM<T>>)MutableCollectionX.super.combinations();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> ListX<U> cast(Class<U> type) {
		
		return (ListX<U>)MutableCollectionX.super.cast(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#when(com.aol.cyclops.matcher.Case[])
	 */
	@Override
	default <R> ListX<Optional<R>> when(Case<T, R, Function<T, R>>... cases) {
		
		return (ListX<Optional<R>>)MutableCollectionX.super.when(cases);
	}

	
	
}
