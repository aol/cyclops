package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.HashTreePSet;
import org.pcollections.PQueue;
import org.pcollections.PSet;

import com.aol.cyclops.collections.PQueues;
import com.aol.cyclops.collections.PSets;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PQueueX<T> extends PQueue<T>, PersistentCollectionX<T>{

	public static <T> PQueueX<T> of(T...values){
		
		return new PQueueXImpl<>(PQueues.of(values));
	}
	public static <T> PQueueX<T> empty(){
		return new PQueueXImpl<>(PQueues .empty());
	}
	public static <T> PQueueX<T> singleton(T value){
		return new PQueueXImpl<>(PQueues.singleton(value));
	}
	public static<T> PQueueX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PQueue)
			return new PQueueXImpl<>((PQueue)(stream));
		return new PQueueXImpl<>(PQueues.fromCollection(stream));
	}
	public static<T> PQueueX<T> fromStream(Stream<T> stream){
		return new PQueueXImpl<>((PQueue<T>)PSets.toPSet().mapReduce(stream));
	}
	
	default PQueue<T> toPSet(){
		return this;
	}
	
	default <X> PQueueX<X> from(Collection<X> col){
		return new PQueueXImpl<>(PQueues.fromCollection(col));
	}
	default <T> Monoid<PQueue<T>> monoid(){
		return PQueues.toPQueue();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public PQueueX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public PQueueX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public PQueueX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public PQueueX<T> minusAll(Collection<?> list);

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PQueueX<T> reverse() {
		return (PQueueX<T>)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PQueueX<T> filter(Predicate<? super T> pred) {
		return (PQueueX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PQueueX<R> map(Function<? super T, ? extends R> mapper) {
		return (PQueueX<R>)PersistentCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PQueueX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		return (PQueueX<R>)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PQueueX<T> limit(long num) {
		return (PQueueX<T>)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PQueueX<T> skip(long num) {
		return (PQueueX<T>)PersistentCollectionX.super.skip(num);
	}
	default PQueueX<T> takeRight(int num){
		return (PQueueX<T>)PersistentCollectionX.super.takeRight(num);
	}
	default PQueueX<T> dropRight(int num){
		return (PQueueX<T>)PersistentCollectionX.super.dropRight(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PQueueX<T> takeWhile(Predicate<? super T> p) {
		return (PQueueX<T>)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PQueueX<T> dropWhile(Predicate<? super T> p) {
		return (PQueueX<T>)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PQueueX<T> takeUntil(Predicate<? super T> p) {
		return (PQueueX<T>)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PQueueX<T> dropUntil(Predicate<? super T> p) {
		return (PQueueX<T>)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PQueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (PQueueX<R>)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PQueueX<T> slice(long from, long to) {
		return (PQueueX<T>)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PQueueX<T> sorted(Function<? super T, ? extends U> function) {
		return (PQueueX<T>)PersistentCollectionX.super.sorted(function);
	}
	

}
