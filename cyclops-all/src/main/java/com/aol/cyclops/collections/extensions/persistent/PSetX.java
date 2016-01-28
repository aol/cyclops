package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.PSet;

import com.aol.cyclops.collections.PSets;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PSetX<T> extends PSet<T>, PersistentCollectionX<T>{

	default PSet<T> toPSet(){
		return this;
	}
	
	default <X> PSetX<X> from(Collection<X> col){
		return PSets.fromCollection(col);
	}
	default <T> Monoid<PSet<T>> monoid(){
		return PSets.toPSet();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public PSetX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public PSetX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public PSetX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public PSetX<T> minusAll(Collection<?> list);

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PSetX<T> reverse() {
		return (PSetX<T>)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> filter(Predicate<? super T> pred) {
		return (PSetX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> map(Function<? super T, ? extends R> mapper) {
		return (PSetX<R>)PersistentCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return (PSetX<R>)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PSetX<T> limit(long num) {
		return (PSetX<T>)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PSetX<T> skip(long num) {
		return (PSetX<T>)PersistentCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> takeWhile(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> dropWhile(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> takeUntil(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> dropUntil(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (PSetX<R>)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PSetX<T> slice(long from, long to) {
		return (PSetX<T>)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PSetX<T> sorted(Function<? super T, ? extends U> function) {
		return (PSetX<T>)PersistentCollectionX.super.sorted(function);
	}
	

}
