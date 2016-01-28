package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.PSet;
import org.pcollections.PVector;

import com.aol.cyclops.collections.PSets;
import com.aol.cyclops.collections.PVectors;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PVectorX<T> extends PVector<T>, PersistentCollectionX<T>{

	default PVector<T> toPVector(){
		return this;
	}
	
	default <X> PVectorX<X> from(Collection<X> col){
		return PVectors.fromCollection(col);
	}
	default <T> Monoid<PVector<T>> monoid(){
		return PVectors.toPVector();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public PVectorX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public PVectorX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public PVectorX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public PVectorX<T> minusAll(Collection<?> list);

	

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
	

}
