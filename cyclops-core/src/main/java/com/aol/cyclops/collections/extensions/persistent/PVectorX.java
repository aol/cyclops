package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.ConsPStack;
import org.pcollections.PQueue;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.collections.PQueues;
import com.aol.cyclops.collections.PVectors;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PVectorX<T> extends PVector<T>, PersistentCollectionX<T>{
	//after module merge, move to reducers
	public static <T> Monoid<PVectorX<T>> toPVectorX() { 
			return	Monoid.<PVectorX<T>>of(PVectorX.empty(), 
									(PVectorX<T> a) -> b -> a.plusAll(b),
									(T x) -> PVectorX.singleton(x));
	}
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
	
	default PVector<T> toPVector(){
		return this;
	}
	
	default <X> PVectorX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Monoid<PVector<T>> monoid(){
		return PVectors.toPVector();
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
}
