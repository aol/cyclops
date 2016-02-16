package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.Matchable.CheckValues;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.PVectors;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicative;

public interface PVectorX<T> extends PVector<T>, PersistentCollectionX<T>{
	
	/**
	 * Construct a PVector from the provided values 
	 * 
	 * <pre>
	 * {@code 
	 *  List<String> list = PVectors.of("a","b","c");
	 *  
	 *  // or
	 *  
	 *  PVector<String> list = PVectors.of("a","b","c");
	 *  
	 *  
	 * }
	 * </pre>
	 * 
	 * 
	 * @param values To add to PVector
	 * @return new PVector
	 */
	public static <T> PVectorX<T> of(T...values){
		return new PVectorXImpl<>(TreePVector.from(Arrays.asList(values)));
	}
	/**
	 * <pre>
	 * {@code 
	 *     List<String> empty = PVectors.empty();
	 *    //or
	 *    
	 *     PVector<String> empty = PVectors.empty();
	 * }
	 * </pre>
	 * @return an empty PVector
	 */
	public static<T> PVectorX<T> empty(){
		return new PVectorXImpl<>(TreePVector .empty());
	}
	/**
	 * Construct a PVector containing a single value
	 * </pre>
	 * {@code 
	 *    List<String> single = PVectors.singleton("1");
	 *    
	 *    //or
	 *    
	 *    PVector<String> single = PVectors.singleton("1");
	 * 
	 * }
	 * </pre>
	 * 
	 * @param value Single value for PVector
	 * @return PVector with a single value
	 */
	public static <T> PVectorX<T> singleton(T value){
		return new PVectorXImpl<>(TreePVector.singleton(value));
	}
	public static<T> PVectorX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PVectorX)
			return (PVectorX)iterable;
		if(iterable instanceof PVector)
			return new PVectorXImpl<>((PVector)(iterable));
		PVector<T> res = PVectors.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PVectorXImpl<>(res);
	}
	/**
	 * Create a PVector from the supplied Colleciton
	 * <pre>
	 * {@code 
	 *   PVector<Integer> list = PVectors.fromCollection(Arrays.asList(1,2,3));
	 *   
	 * }
	 * </pre>
	 * 
	 * @param values to add to new PVector
	 * @return PVector containing values
	 */
	public static <T> PVectorX<T> fromCollection(Collection<T> values){
		if(values instanceof PVectorX)
			return (PVectorX)values;
		if(values instanceof PVector)
			return new PVectorXImpl<>((PVector)values);
		return new PVectorXImpl<>(TreePVector.from(values));
	}
	/**
	 * Reduce (immutable Collection) a Stream to a PVector
	 * 
	 * <pre>
	 * {@code 
	 *    PVector<Integer> list = PVectors.fromStream(Stream.of(1,2,3));
	 * 
	 *  //list = [1,2,3]
	 * }</pre>
	 * 
	 * 
	 * @param stream to convert to a PVector
	 * @return
	 */
	public static<T> PVectorX<T> fromStream(Stream<T> stream){
		return new PVectorXImpl<>((PVector<T>)PVectors.toPVector().mapReduce(stream));
	}
	@Override
	default <R> PVectorX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (PVectorX<R>)PersistentCollectionX.super.ap1(ap);
	}
	default PVector<T> toPVector(){
		return this;
	}
	
	default <X> PVectorX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Reducer<PVector<T>> monoid(){
		return PVectors.toPVector();
	}
	@Override
	default PVectorX<T> toPVectorX() {
		return this;
	}
	
	

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PVectorX<T> reverse() {
		return (PVectorX<T>)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> filter(Predicate<? super T> pred) {
		return (PVectorX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PVectorX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (PVectorX<R>)PersistentCollectionX.super.map(mapper);
	}
	@Override
	default<R> PVectorX<R> unit(Collection<R> col){
		return fromCollection(col);
	}
	@Override
	default <R> PVectorX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default<R> PVectorX<R> emptyUnit(){
		return empty();
	}
	@Override
	default <R> PVectorX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PVectorX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		
		return (PVectorX<R>)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PVectorX<T> limit(long num) {
		return (PVectorX<T>)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PVectorX<T> skip(long num) {
		return (PVectorX<T>)PersistentCollectionX.super.skip(num);
	}
	default PVectorX<T> takeRight(int num){
		return (PVectorX<T>)PersistentCollectionX.super.takeRight(num);
	}
	default PVectorX<T> dropRight(int num){
		return (PVectorX<T>)PersistentCollectionX.super.dropRight(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> takeWhile(Predicate<? super T> p) {
		return (PVectorX<T>)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> dropWhile(Predicate<? super T> p) {
		return (PVectorX<T>)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> takeUntil(Predicate<? super T> p) {
		return (PVectorX<T>)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> dropUntil(Predicate<? super T> p) {
		return (PVectorX<T>)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PVectorX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (PVectorX<R>)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PVectorX<T> slice(long from, long to) {
		return (PVectorX<T>)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PVectorX<T> sorted(Function<? super T, ? extends U> function) {
		return (PVectorX<T>)PersistentCollectionX.super.sorted(function);
	}
	
	public PVectorX<T> plus(T e);
	
	
	public PVectorX<T> plusAll(Collection<? extends T> list);
	
	public PVectorX<T> with(int i, T e);
	
	
	public PVectorX<T> plus(int i, T e);
	
	
	public PVectorX<T> plusAll(int i, Collection<? extends T> list);
	
	
	public PVectorX<T> minus(Object e);
	
	
	public PVectorX<T> minusAll(Collection<?> list);
	
	
	public PVectorX<T> minus(int i);

	
	public PVectorX<T> subList(int start, int end);
	
	default PVectorX<ListX<T>> grouped(int groupSize){
		return  (PVectorX<ListX<T>>)PersistentCollectionX.super.grouped(groupSize);
	}
	default <K, A, D> PVectorX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return  (PVectorX)PersistentCollectionX.super.grouped(classifier,downstream);
	}
	default <K> PVectorX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return  (PVectorX)PersistentCollectionX.super.grouped(classifier);
	}
	default <U> PVectorX<Tuple2<T, U>> zip(Iterable<U> other){
		return  (PVectorX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> PVectorX<R> zip(Iterable<U> other,
			BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (PVectorX<R>)PersistentCollectionX.super.zip(other, zipper);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#permutations()
	 */
	@Override
	default PVectorX<ReactiveSeq<T>> permutations() {
		
		return ( PVectorX<ReactiveSeq<T>>)PersistentCollectionX.super.permutations();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations(int)
	 */
	@Override
	default PVectorX<ReactiveSeq<T>> combinations(int size) {
		
		return (PVectorX<ReactiveSeq<T>>)PersistentCollectionX.super.combinations(size);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations()
	 */
	@Override
	default PVectorX<ReactiveSeq<T>> combinations() {
		
		return (PVectorX<ReactiveSeq<T>>)PersistentCollectionX.super.combinations();
	}

	default PVectorX<ListX<T>> sliding(int windowSize){
		return  (PVectorX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize);
	}
	default PVectorX<ListX<T>> sliding(int windowSize, int increment){
		return  (PVectorX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize,increment);
	}
	default PVectorX<T> scanLeft(Monoid<T> monoid){
		return  (PVectorX<T>)PersistentCollectionX.super.scanLeft(monoid);
	}
	default <U> PVectorX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return  (PVectorX<U>)PersistentCollectionX.super.scanLeft(seed,function);
	}
	default PVectorX<T> scanRight(Monoid<T> monoid){
		return  (PVectorX<T>)PersistentCollectionX.super.scanRight(monoid);
	}
	default <U> PVectorX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return  (PVectorX<U>)PersistentCollectionX.super.scanRight(identity,combiner);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
	 */
	@Override
	default PVectorX<T> plusInOrder(T e) {
		
		return (PVectorX<T>)PersistentCollectionX.super.plusInOrder(e);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(int)
	 */
	@Override
	default PVectorX<T> cycle(int times) {
		
		return (PVectorX<T>)PersistentCollectionX.super.cycle(times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default PVectorX<T> cycle(Monoid<T> m, int times) {
		
		return (PVectorX<T>)PersistentCollectionX.super.cycle(m, times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (PVectorX<T>)PersistentCollectionX.super.cycleWhile(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (PVectorX<T>)PersistentCollectionX.super.cycleUntil(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> PVectorX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (PVectorX<Tuple2<T, U>>)PersistentCollectionX.super.zipStream(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> PVectorX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (PVectorX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> PVectorX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (PVectorX)PersistentCollectionX.super.zip3(second, third);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> PVectorX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (PVectorX<Tuple4<T, T2, T3, T4>>)PersistentCollectionX.super.zip4(second, third, fourth);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
	 */
	@Override
	default PVectorX<Tuple2<T, Long>> zipWithIndex() {
		
		return (PVectorX<Tuple2<T, Long>>)PersistentCollectionX.super.zipWithIndex();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#distinct()
	 */
	@Override
	default PVectorX<T> distinct() {
		
		return (PVectorX<T>)PersistentCollectionX.super.distinct();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted()
	 */
	@Override
	default PVectorX<T> sorted() {
		
		return (PVectorX<T>)PersistentCollectionX.super.sorted();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default PVectorX<T> sorted(Comparator<? super T> c) {
		
		return (PVectorX<T>)PersistentCollectionX.super.sorted(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> skipWhile(Predicate<? super T> p) {
		
		return (PVectorX<T>)PersistentCollectionX.super.skipWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> skipUntil(Predicate<? super T> p) {
		
		return (PVectorX<T>)PersistentCollectionX.super.skipUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> limitWhile(Predicate<? super T> p) {
		
		return (PVectorX<T>)PersistentCollectionX.super.limitWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> limitUntil(Predicate<? super T> p) {
		
		return (PVectorX<T>)PersistentCollectionX.super.limitUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default PVectorX<T> intersperse(T value) {
		
		return (PVectorX<T>)PersistentCollectionX.super.intersperse(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle()
	 */
	@Override
	default PVectorX<T> shuffle() {
		
		return (PVectorX<T>)PersistentCollectionX.super.shuffle();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
	 */
	@Override
	default PVectorX<T> skipLast(int num) {
		
		return (PVectorX<T>)PersistentCollectionX.super.skipLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
	 */
	@Override
	default PVectorX<T> limitLast(int num) {
		
		return (PVectorX<T>)PersistentCollectionX.super.limitLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default PVectorX<T> onEmpty(T value) {
		
		return (PVectorX<T>)PersistentCollectionX.super.onEmpty(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default PVectorX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (PVectorX<T>)PersistentCollectionX.super.onEmptyGet(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> PVectorX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (PVectorX<T>)PersistentCollectionX.super.onEmptyThrow(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default PVectorX<T> shuffle(Random random) {
		
		return (PVectorX<T>)PersistentCollectionX.super.shuffle(random);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> PVectorX<U> ofType(Class<U> type) {
		
		return (PVectorX<U>)PersistentCollectionX.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default PVectorX<T> filterNot(Predicate<? super T> fn) {
		
		return (PVectorX<T>)PersistentCollectionX.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#notNull()
	 */
	@Override
	default PVectorX<T> notNull() {
		
		return (PVectorX<T>)PersistentCollectionX.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default PVectorX<T> removeAll(Stream<T> stream) {
		
		return (PVectorX<T>)PersistentCollectionX.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default PVectorX<T> removeAll(Iterable<T> it) {
		
		return (PVectorX<T>)PersistentCollectionX.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default PVectorX<T> removeAll(T... values) {
		
		return (PVectorX<T>)PersistentCollectionX.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default PVectorX<T> retainAll(Iterable<T> it) {
		
		return (PVectorX<T>)PersistentCollectionX.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default PVectorX<T> retainAll(Stream<T> stream) {
		
		return (PVectorX<T>)PersistentCollectionX.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default PVectorX<T> retainAll(T... values) {
		
		return (PVectorX<T>)PersistentCollectionX.super.retainAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PVectorX<T> retainMatches(Matcher<T> m) {
		
		return (PVectorX<T>)PersistentCollectionX.super.retainMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PVectorX<T> removeMatches(Matcher<T> m) {
		
		return (PVectorX<T>)PersistentCollectionX.super.removeMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> PVectorX<U> cast(Class<U> type) {
		
		return (PVectorX<U>)PersistentCollectionX.super.cast(type);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> PVectorX<R> patternMatch(
			Function<CheckValues<T, R>, CheckValues<T, R>> case1,Supplier<? extends R> otherwise) {
		
		return (PVectorX<R>)PersistentCollectionX.super.patternMatch(case1,otherwise);
	}
	
}
