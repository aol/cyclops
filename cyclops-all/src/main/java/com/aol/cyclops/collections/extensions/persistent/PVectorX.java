package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.PSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.collections.PSets;
import com.aol.cyclops.collections.PVectors;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

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

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PVectorX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
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
}
